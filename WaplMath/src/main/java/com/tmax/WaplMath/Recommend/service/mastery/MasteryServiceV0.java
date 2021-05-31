package com.tmax.WaplMath.Recommend.service.mastery;

import java.io.IOException;
import java.io.Reader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.tmax.WaplMath.Problem.repository.ProblemRepository;
import com.tmax.WaplMath.Recommend.common.MasteryAPIManager;
import com.tmax.WaplMath.Recommend.model.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.model.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.model.UserEmbedding;
import com.tmax.WaplMath.Recommend.model.UserKnowledge;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepository;
import com.tmax.WaplMath.Recommend.repository.UserEmbeddingRepository;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;

@Service("MasteryServiceV0")
@Primary
public class MasteryServiceV0 implements MasteryServiceBase {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	MasteryAPIManager masteryAPIManager = new MasteryAPIManager();

	@Autowired
	UserEmbeddingRepository userEmbeddingRepository;

	@Autowired
	ProblemUkRelRepository problemUkRelRepository;

	@Autowired
	ProblemRepository problemRepository;

	@Autowired
	UserKnowledgeRepository userKnowledgeRepository;

	@Override
	public ResultMessageDTO updateMastery(String userId, List<String> probIdList, List<String> correctList) {
		ResultMessageDTO output = new ResultMessageDTO();
		
		System.out.println("userId: " + userId);
		System.out.println("probIdList: " + probIdList);
		System.out.println("correctList: " + correctList);
		
		String userEmbedding = "";

		// input validation check
		if (probIdList.size() == 0) {
			output.setMessage("Size of probIdList must be greater than 0.");
			return output;
		} else if (probIdList.size() != correctList.size()) {
			output.setMessage("Size of probIdList must be equal to size of correctList.");
			return output;
		}

		// check whether user embedding saved in UserEmbedding TB or not
		logger.info("Get user embedding...");
		Optional<UserEmbedding> userEmbeddingOptional = userEmbeddingRepository.findById(userId);
		if (userEmbeddingOptional.isPresent())
			userEmbedding = userEmbeddingOptional.get().getUserEmbedding();

		logger.info("User embedding input length = " + Integer.toString(userEmbedding.length()));
		
		// generate triton server input: probId --> ukId
		List<String> ukIdList = new ArrayList<String>();
		List<String> corList = new ArrayList<String>();
		List<String> levelList = new ArrayList<String>();

		for (int i = 0; i < probIdList.size(); i++) {
			String probId = probIdList.get(i);
			String correctness = correctList.get(i);
			Integer probIdInt;
			try {
				probIdInt = Integer.parseInt(probId);
			} catch (NumberFormatException e) {
				output.setMessage("Problem Id Parse Exception: " + e.getMessage());
				return output;
			}
			// add ukList, corList, levelList
			List<Integer> probUkIdList = problemUkRelRepository.findAllUkIdByProbId(probIdInt);
			String difficulty = problemRepository.findByProbId(probIdInt).getDifficulty();
			if (difficulty == null) {
				output.setMessage(String.format("probId=%d is not valid problem Id.", probIdInt));
				return output;
			}
			probUkIdList.forEach((ukId) -> {
				ukIdList.add(Integer.toString(ukId));
				corList.add(correctness);
				levelList.add(difficulty);
			});
		}

		// Triton server HTTP request/response
		JsonObject tritonOutput;
		try {
			tritonOutput = masteryAPIManager.measureMastery(userId, ukIdList, corList, levelList, userEmbedding);
		} catch (Exception e) {
			output.setMessage("Triton Internal Server Error: " + e.getMessage());
			return output;
		}
		JsonObject masteryJson = tritonOutput.get("Mastery").getAsJsonObject();
		userEmbedding = tritonOutput.get("Embeddings").toString();
		logger.info("User embedding output length = " + Integer.toString(userEmbedding.length()));

		// update user mastery
		logger.info("Update mastery of user...");
		Set<UserKnowledge> userKnowledgeSet = new HashSet<UserKnowledge>();

		masteryJson.keySet().forEach(ukId -> {
			UserKnowledge userKnowledge = new UserKnowledge();
			userKnowledge.setUserUuid(userId);
			userKnowledge.setUkId(Integer.parseInt(ukId));
			userKnowledge.setUkMastery(masteryJson.get(ukId).getAsFloat());
			userKnowledge.setUpdateDate(Timestamp.valueOf(LocalDateTime.now()));
			userKnowledgeSet.add(userKnowledge);
		});
		userKnowledgeRepository.saveAll(userKnowledgeSet);

		// update user embedding
		logger.info("Update embedding of user...");
		UserEmbedding updateEmbedding = new UserEmbedding();
		updateEmbedding.setUserUuid(userId);
		updateEmbedding.setUserEmbedding(userEmbedding);
		updateEmbedding.setUpdateDate(Timestamp.valueOf(LocalDateTime.now()));
		userEmbeddingRepository.save(updateEmbedding);

		output.setMessage("Successfully update user mastery.");
		return output;
	}

}
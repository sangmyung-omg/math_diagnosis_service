package com.tmax.WaplMath.Recommend.service.mastery.v1;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.mastery.TritonMasteryDTO;
import com.tmax.WaplMath.Recommend.event.mastery.MasteryEventPublisher;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.model.knowledge.UserEmbedding;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.model.problem.ProblemUkRel;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepository;
import com.tmax.WaplMath.Recommend.repository.UserEmbeddingRepository;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;
import com.tmax.WaplMath.Recommend.util.MasteryAPIManager;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("MasteryServiceV1")
public class MasteryServiceV1 implements MasteryServiceBaseV1{
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    
    //LRS API Manager
    @Autowired
    LRSAPIManager lrsapiManager;

    @Autowired
    MasteryAPIManager masteryAPIManager;

    @Autowired
	UserEmbeddingRepository userEmbeddingRepo;

	@Autowired
	ProblemUkRelRepository probUkRelRepo;

	@Autowired
	ProblemRepo probRepo;
    
	@Autowired
	UserKnowledgeRepository userKnowledgeRepository;



    //Event publisher
    @Autowired
    MasteryEventPublisher masteryEventPublisher;


    //Scaler for uk mastery
    private static final float SCALE_PARAM = 0.65079f;
	private static final float BIAS_PARAM = 0.4f;
    private static final float SCALE_THRESHOLD = 0.85f;
    
    private float correctUkMastery(float currentMastery){
        return currentMastery >= SCALE_THRESHOLD ? 1.0f : (float)( SCALE_PARAM * Math.sqrt(currentMastery) + BIAS_PARAM);
    }


    @Override
    public ResultMessageDTO updateMastery(String userId, List<String> probIdList, List<String> correctList) {
        //Throw condition. Major error
        if (correctList.size() != probIdList.size()){
            throw new RecommendException(RecommendErrorCode.DATA_MISMATCH_ERROR,  "List size does not match");
        }
        
        //Pass condition check (any of the two lists' size == 0 or size doesn't match)
        if(probIdList.size() == 0 || correctList.size() == 0){
            return new ResultMessageDTO("Nothing to update");
        }

        //Convert probIdList to IntList
        List<Integer> probIdIntList = new ArrayList<>();
        for(String probId : probIdList){probIdIntList.add(Integer.parseInt(probId));}

        //Build List difficulty and UK id from probIDList
        logger.info("ProbList data: " + probIdList.toString());
        List<ProblemUkRel> probUKList = probUkRelRepo.getByProblemIDList(probIdIntList);

        //order list by probIDList order (Map<Prob ID, List<UK ID> >) 
        Map<Integer, List<Integer>> probIDUKMap = new HashMap<>();

        //Fill the map
        probUKList.forEach(puk -> {
            //Declare list placeholder
            List<Integer> ukList = null;

            //If nothing exists
            if(!probIDUKMap.containsKey(puk.getProbId())){
                //Create new List and add current ukId
                ukList = new ArrayList<>();
            }
            else {
                //Get the existing UKList
                ukList = probIDUKMap.get(puk.getProbId());
            }
            
            ukList.add(puk.getUkId());
            probIDUKMap.put(puk.getProbId(), ukList);
        });
        
        //Get the difficulty info of problems
        List<Problem> probDataList = probRepo.getProblemsByProbIdList(probIdIntList);
        
        //Build probId --> info map
        Map<Integer, String> probDiffMap = new HashMap<>();
        probDataList.forEach(prob->probDiffMap.put(prob.getProbId(), prob.getDifficulty()));

        //Using the map + probIDList(ordered), build the lists required for the mastery api manager
        List<String> ukIDList = new ArrayList<>();
        List<String> ukCorrectList = new ArrayList<>();
        List<String> diffList = new ArrayList<>();

        //Build the list
        int index = 0;
        for(String probId : probIdList){
            //Current correctness and diff level
            String correct = correctList.get(index++);
            String diffLevel = probDiffMap.get(Integer.parseInt(probId));

            //Get the UK List of the probId
            List<Integer> ukList = probIDUKMap.get(Integer.parseInt(probId));

            //Log
            logger.info("correct: " +  correct + ", diff: " + diffLevel + ", probId: " + probId);

            if(ukList == null){
                continue;
            }

            //For each ukList. push the values to list            
            ukList.forEach(uk -> {
                ukIDList.add(uk.toString());
                ukCorrectList.add(correct);
                diffList.add(diffLevel);
            });
        };


        //Get the current embedding
        UserEmbedding userEmbedding = userEmbeddingRepo.getEmbedding(userId);
        String embeddingStr = userEmbedding != null ? userEmbedding.getUserEmbedding() : "";

        //Measure the current mastery
        TritonMasteryDTO tritonMastery = masteryAPIManager.measureMasteryDTO(userId, ukIDList, ukCorrectList, diffList, embeddingStr);


        //Update or insert the new mastery to DB 
        //userEmbeddingRepo.updateEmbedding(userId, tritonMastery.getEmbedding());
        userEmbeddingRepo.save(UserEmbedding.builder()
                                            .userUuid(userId)
                                            .userEmbedding(tritonMastery.getEmbedding())
                                            .updateDate(Timestamp.valueOf(LocalDateTime.now()))
                                            .build());

        //Save each UK data to UK table
        Set<UserKnowledge> updateUKSet = new HashSet<>();
        for(Map.Entry<Integer, Float> ukEntry: tritonMastery.getMastery().entrySet()){
            //Builder pattern for user knowledge
            UserKnowledge uknow = UserKnowledge.builder()
                                    .userUuid(userId)
                                    .ukId(ukEntry.getKey())
                                    .ukMastery(correctUkMastery(ukEntry.getValue()))
                                    .updateDate(Timestamp.valueOf(LocalDateTime.now()))
                                    .build();   

            updateUKSet.add(uknow);
        }
        userKnowledgeRepository.saveAll(updateUKSet);


        //Propagate update event to inform mastery update
        masteryEventPublisher.publishChangeEvent(userId);

        return new ResultMessageDTO("Update successfull");
    }

    @Override
    public ResultMessageDTO updateMasteryFromLRS(String token) {
        ProblemSolveListDTO result =  lrsapiManager.getLRSUpdateProblemSequence(token);
        String userID = JWTUtil.getJWTPayloadField(token, "userID");

        return this.updateMastery(userID, result.getProbIdList(), result.getCorrectList());
    }

}

package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.dto.StatementDTO;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepository;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

/**
 * Get schedule card solved history from LRS
 * @author Sangheon_lee
 */
@Component
public class ScheduleHistoryManagerV0 {

	// Constant
	private final Integer MAX_RECENET_STATEMENT_NUM = 100;

	@Autowired
	ProblemRepo problemRepo;
	@Autowired
	ProblemUkRelRepository probUkRelRepo;
	@Autowired
	LRSAPIManager lrsAPIManager = new LRSAPIManager();

	public Set<Integer> getCompletedProbIdList(String userId, String today, List<String> sourceTypeList) throws Exception {
		Set<Integer> probIdList = new HashSet<Integer>();
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		List<StatementDTO> LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateTo(today);
		LRSinput.setSourceTypeList(sourceTypeList);
		LRSinput.setActionTypeList(new ArrayList<String>(Arrays.asList("submit")));
		LRSinput.setRecentStatementNum(MAX_RECENET_STATEMENT_NUM);

		try {
			LRSResult = lrsAPIManager.getStatementListNew(LRSinput);
		} catch (Exception e) {
			throw new Exception("LRS Internal Server Error: " + e.getMessage());
		}
		if (LRSResult.size() != 0) {
			for (StatementDTO statement : LRSResult) {
				String sourceId = statement.getSourceId();
				try {
					probIdList.add(Integer.parseInt(sourceId));
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage() + " is not number.");
				}
			}
		}
		return probIdList;
	}

	public List<Integer> getCompletedTypeIdList(String userId, String today) throws Exception {
		Set<Integer> probIdList;
		try {
			probIdList = getCompletedProbIdList(userId, today, new ArrayList<String>(Arrays.asList("type_question")));
		} catch (Exception e) {
			throw e;
		}
		List<Integer> typeIdList = new ArrayList<Integer>();
		if (probIdList.size() != 0)
			typeIdList = problemRepo.findTypeIdList(probIdList);
		return typeIdList;
	}

	public List<String> getCompletedSectionIdList(String userId, String today) throws Exception {
		Set<Integer> probIdList;
		try {
			probIdList = getCompletedProbIdList(userId, today, new ArrayList<String>(Arrays.asList("mid_exam_question")));
		} catch (Exception e) {
			throw e;
		}
		List<String> sectionIdList = new ArrayList<String>();
		if (probIdList.size() != 0)
			sectionIdList = problemRepo.findSectionIdList(probIdList);
		return sectionIdList;
	}

	public List<Integer> getCompletedSuppleUkIdList(String userId, String today) throws Exception {
		Set<Integer> probIdList;
		try {
			probIdList = getCompletedProbIdList(userId, today, new ArrayList<String>(Arrays.asList("supple_question")));
		} catch (Exception e) {
			throw e;
		}
		List<Integer> ukIdList = new ArrayList<Integer>();
		if (probIdList.size() != 0)
			ukIdList = probUkRelRepo.findUkIdList(probIdList);
		return ukIdList;
	}

	public List<Integer> getSolvedUkIdList(String userId, String today) throws Exception {
		Set<Integer> probIdList;
		try {
			probIdList = getCompletedProbIdList(userId, today,
				new ArrayList<String>(Arrays.asList("type_question", "mid_exam_question", "trial_exam_question")));
		} catch (Exception e) {
			throw e;
		}
		List<Integer> ukIdList = new ArrayList<Integer>();
		if (probIdList.size() != 0)
			ukIdList = probUkRelRepo.findUkIdList(probIdList);
		return ukIdList;
	}
}

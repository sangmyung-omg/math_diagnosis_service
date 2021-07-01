package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepository;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

/**
 * Get schedule card solved history from LRS
 * @author Sangheon_lee
 */
@Component
public class ScheduleHistoryManagerV1 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	// Constant
	private final Integer MAX_RECENET_STATEMENT_NUM = 100;

	@Autowired
	ProblemRepo problemRepo;
	@Autowired
	ProblemUkRelRepository probUkRelRepo;
	@Autowired
	LRSAPIManager lrsAPIManager = new LRSAPIManager();

	public Set<Integer> getCompletedProbIdSet(String userId, String today, String dateFrom, List<String> sourceTypeList) throws Exception {
		Set<Integer> probIdSet = new HashSet<Integer>();
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		JsonArray LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateTo(today);
		if (dateFrom != "")
			LRSinput.setDateFrom(dateFrom);
		LRSinput.setSourceTypeList(sourceTypeList);
		LRSinput.setActionTypeList(new ArrayList<String>(Arrays.asList("submit")));
		LRSinput.setRecentStatementNum(MAX_RECENET_STATEMENT_NUM);

		try {
			LRSResult = lrsAPIManager.getStatementList(LRSinput);
		} catch (Exception e) {
			throw new Exception("LRS Internal Server Error: " + e.getMessage());
		}
		if (LRSResult.size() != 0) {
			for (JsonElement rowElement : LRSResult) {
				JsonObject row = (JsonObject) rowElement;
				String sourceId = row.get("sourceId").getAsString();
				try {
					probIdSet.add(Integer.parseInt(sourceId));
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage() + " is not number.");
				}
			}
		}
		return probIdSet;
	}


	public String getRecentSuppleCardDate(String userId, String today) throws Exception {
		String date;
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		JsonArray LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateTo(today);
		LRSinput.setSourceTypeList(new ArrayList<String>(Arrays.asList("supple_question")));
		LRSinput.setActionTypeList(new ArrayList<String>(Arrays.asList("submit")));
		LRSinput.setRecentStatementNum(1);

		try {
			LRSResult = lrsAPIManager.getStatementList(LRSinput);
		} catch (Exception e) {
			throw new Exception("LRS Internal Server Error: " + e.getMessage());
		}
		if (LRSResult.size() == 0)
			return "";
		else {
			JsonObject row = (JsonObject) LRSResult.get(0);
			String timestamp = row.get("timestamp").getAsString();
			date = timestamp.substring(0, 10);
		}
		return date;
	}

	public List<Integer> getCompletedTypeIdList(String userId, String today, String dateFrom, String sourceType) throws Exception {
		Set<Integer> probIdSet;
		try {
			probIdSet = getCompletedProbIdSet(userId, today, dateFrom, new ArrayList<String>(Arrays.asList(sourceType)));
		} catch (Exception e) {
			throw e;
		}
		List<Integer> typeIdList = new ArrayList<Integer>();
		if (probIdSet.size() != 0)
			typeIdList = problemRepo.findTypeIdList(probIdSet);
		return typeIdList;
	}

	//21.07.01 card generator v2
	public List<Integer> getCompletedTypeIdListAfterSuppleCard(String userId, String today) throws Exception {
		String lastSuppleDate;
		try {
			lastSuppleDate = getRecentSuppleCardDate(userId, today);
		} catch (Exception e) {
			throw e;
		}
		logger.info("가장 최근에 푼 보충카드 날짜: " + lastSuppleDate);
		return getCompletedTypeIdList(userId, today, lastSuppleDate, "type-question");
	}

	public List<String> getCompletedSectionCardIdList(String userId, String today) throws Exception {
		Set<Integer> probIdSet;
		try {
			probIdSet = getCompletedProbIdSet(userId, today, "", new ArrayList<String>(Arrays.asList("mid_exam_question")));
		} catch (Exception e) {
			throw e;
		}
		List<String> sectionIdList = new ArrayList<String>();
		if (probIdSet.size() != 0)
			sectionIdList = problemRepo.findSectionIdList(probIdSet);
		return sectionIdList;
	}

	public List<Integer> getSolvedUkIdList(String userId, String today) throws Exception {
		Set<Integer> probIdSet;
		try {
			probIdSet = getCompletedProbIdSet(userId, today, "",
				new ArrayList<String>(Arrays.asList("type_question", "mid_exam_question", "trial_exam_question")));
		} catch (Exception e) {
			throw e;
		}
		List<Integer> ukIdList = new ArrayList<Integer>();
		if (probIdSet.size() != 0)
			ukIdList = probUkRelRepo.findUkIdList(probIdSet);
		return ukIdList;
	}
}

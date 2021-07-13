package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.dto.StatementDTO;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepository;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

/**
 * Get schedule card solved history from LRS
 * @author Sangheon_lee
 */
@Slf4j
@Component
public class ScheduleHistoryManagerV1 {

	// Constant
	private final Integer MAX_RECENET_STATEMENT_NUM = 200;

	@Autowired
	ProblemRepo problemRepo;
	@Autowired
	ProblemUkRelRepository probUkRelRepo;
	@Autowired
	LRSAPIManager lrsAPIManager = new LRSAPIManager();

	public Set<Integer> getSolvedProbIdSet(String userId, String today, String dateFrom, List<String> sourceTypeList) throws Exception {
		Set<Integer> probIdSet = new HashSet<Integer>();
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		List<StatementDTO> LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateTo(today);
		if (dateFrom != "")
			LRSinput.setDateFrom(dateFrom);
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
					probIdSet.add(Integer.parseInt(sourceId));
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage() + " is not number.");
				}
			}
		}
		return probIdSet;
	}

	public Map<String, Set<Integer>> getSolvedProbIdSetByDay(String userId, String today, String dateFrom, List<String> sourceTypeList) throws Exception {
		Map<String, Set<Integer>> dayProbIdSet = new HashMap<String, Set<Integer>>();
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		List<StatementDTO> LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateTo(today);
		if (dateFrom != "")
			LRSinput.setDateFrom(dateFrom);
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
				String date = statement.getTimestamp().substring(0, 10);;
				Set<Integer> probIdSet;
				try {
					if(!dayProbIdSet.containsKey(date)) {
						probIdSet = new HashSet<Integer>();
					}else {
						probIdSet = dayProbIdSet.get(date);
					}
					probIdSet.add(Integer.parseInt(sourceId));
					dayProbIdSet.put(date, probIdSet);
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage() + " is not number.");
				}
			}
		}
		return dayProbIdSet;
	}

	public String getRecentSuppleCardDate(String userId, String today) throws Exception {
		String date;
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		List<StatementDTO> LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateTo(today);
		LRSinput.setSourceTypeList(new ArrayList<String>(Arrays.asList("supple_question")));
		LRSinput.setActionTypeList(new ArrayList<String>(Arrays.asList("submit")));
		LRSinput.setRecentStatementNum(1);

		try {
			LRSResult = lrsAPIManager.getStatementListNew(LRSinput);
		} catch (Exception e) {
			throw new Exception("LRS Internal Server Error: " + e.getMessage());
		}
		if (LRSResult.size() == 0)
			return "";
		else {
			StatementDTO statement = LRSResult.get(0);
			date = statement.getTimestamp().substring(0, 10);
		}
		return date;
	}

	public List<Integer> getCompletedTypeIdList(String userId, String today, String dateFrom, String sourceType) throws Exception {
		Set<Integer> probIdSet;
		try {
			probIdSet = getSolvedProbIdSet(userId, today, dateFrom, new ArrayList<String>(Arrays.asList(sourceType)));
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
		log.info("가장 최근에 푼 보충카드 날짜: " + lastSuppleDate);
		return getCompletedTypeIdList(userId, today, lastSuppleDate, "type-question");
	}

	public Set<String> getCompletedSectionIdList(String userId, String today, String sourceType) throws Exception {
		Set<Integer> probIdSet;
		try {
			probIdSet = getSolvedProbIdSet(userId, today, "", new ArrayList<String>(Arrays.asList(sourceType)));
		} catch (Exception e) {
			throw e;
		}
		Set<String> sectionIdSet = new HashSet<String>();
		if (probIdSet.size() != 0)
			sectionIdSet = problemRepo.findSectionIdSet(probIdSet);
		return sectionIdSet;
	}
	
	public Map<String, Integer> getCompletedSectionNum(String userId, String today, String sourceType) throws Exception{
		Map<String, Set<Integer>> daySolvedProbIdSet;
		try {
			daySolvedProbIdSet = getSolvedProbIdSetByDay(userId, today, "", new ArrayList<String>(Arrays.asList(sourceType)));
		} catch (Exception e) {
			throw e;
		}
		Map<String, Integer> completedType1CardsNum = new HashMap<String, Integer>();
		for(String date: daySolvedProbIdSet.keySet()) {
			Set<Integer> probIdSet = daySolvedProbIdSet.get(date);
			Set<String> daySectionIdSet = problemRepo.findSectionIdSet(probIdSet);
			String sectionId = daySectionIdSet.iterator().next();
			if (!completedType1CardsNum.containsKey(sectionId))
				completedType1CardsNum.put(sectionId, 1);
			else
				completedType1CardsNum.put(sectionId, completedType1CardsNum.get(sectionId)+1);
		}
		return completedType1CardsNum;
	}
}

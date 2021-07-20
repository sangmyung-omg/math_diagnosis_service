package com.tmax.WaplMath.Recommend.util.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.dto.StatementDTO;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;
import com.tmax.WaplMath.Recommend.util.config.CardConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Get schedule card solved history from LRS
 * @author Sangheon_lee
 */
@Slf4j
@Component
public class ScheduleHistoryManagerV1 {


  // Constant
  private static final Integer MAX_RECENET_STATEMENT_NUM = 200;
  private static final String ACTION_SUBMIT = "submit";

  
  @Autowired
  private ProblemRepo problemRepo;

  @Autowired
  LRSAPIManager lrsAPIManager;

  
  public Set<Integer> getSolvedProbIdSet(String userId, String today, String dateFrom, List<String> sourceTypeList){

    GetStatementInfoDTO LRSinput = GetStatementInfoDTO.builder()
                                                      .userIdList(new ArrayList<>(Arrays.asList(userId)))
                                                      .dateTo(today)
                                                      .dateFrom(!dateFrom.equals("") ? dateFrom : null)
                                                      .sourceTypeList(sourceTypeList)
                                                      .actionTypeList(new ArrayList<>(Arrays.asList(ACTION_SUBMIT)))
                                                      .recentStatementNum(MAX_RECENET_STATEMENT_NUM)
                                                      .build();

    List<StatementDTO> LRSResult = lrsAPIManager.getStatementListNew(LRSinput);

    return LRSResult.isEmpty() ? new HashSet<>()
                               : LRSResult.stream()
                                          .map(statement -> Integer.parseInt(statement.getSourceId()))
                                          .collect(Collectors.toSet());
  }


  public Map<String, Set<Integer>> getSolvedProbIdSetByDay(String userId, String today, String dateFrom, 
                                                           List<String> sourceTypeList) {

    Map<String, Set<Integer>> dayProbIdSet = new HashMap<>();

    GetStatementInfoDTO LRSinput = GetStatementInfoDTO.builder()
                                                      .userIdList(new ArrayList<>(Arrays.asList(userId)))
                                                      .dateTo(today)
                                                      .dateFrom(!dateFrom.equals("") ? dateFrom : null)
                                                      .sourceTypeList(sourceTypeList)
                                                      .actionTypeList(new ArrayList<>(Arrays.asList(ACTION_SUBMIT)))
                                                      .recentStatementNum(MAX_RECENET_STATEMENT_NUM)
                                                      .build();

    List<StatementDTO> LRSResult = lrsAPIManager.getStatementListNew(LRSinput);

    if (!LRSResult.isEmpty()) {
      LRSResult.forEach(statement -> {

        String date = statement.getTimestamp().substring(0, 10);
        Set<Integer> probIdSet = dayProbIdSet.containsKey(date) ? dayProbIdSet.get(date) 
                                                                : new HashSet<>();

        probIdSet.add(Integer.parseInt(statement.getSourceId()));
        dayProbIdSet.put(date, probIdSet);
      });
    }
    return dayProbIdSet;
  }


  public String getRecentSuppleCardDate(String userId, String today) {

    GetStatementInfoDTO LRSinput = GetStatementInfoDTO.builder()
                                                      .userIdList(new ArrayList<>(Arrays.asList(userId)))
                                                      .dateTo(today)
                                                      .sourceTypeList(new ArrayList<>(Arrays.asList(CardConstants.SUPPLE_CARD_TYPESTR	+ CardConstants.LRS_SOURCE_TYPE_POSTFIX)))
                                                      .actionTypeList(new ArrayList<>(Arrays.asList(ACTION_SUBMIT)))
                                                      .recentStatementNum(1)
                                                      .build();

    List<StatementDTO> LRSResult = lrsAPIManager.getStatementListNew(LRSinput);

    return LRSResult.isEmpty() ? ""
                               : LRSResult.get(0).getTimestamp().substring(0, 10);
  }


  public List<Integer> getCompletedTypeIdList(String userId, String today, String dateFrom, String sourceType) {

    Set<Integer> probIdSet = getSolvedProbIdSet(userId, today, dateFrom, new ArrayList<>(Arrays.asList(sourceType)));

    return probIdSet.isEmpty() ? new ArrayList<>()
                               : problemRepo.findTypeIdList(probIdSet);
  }


  public Set<String> getCompletedSectionIdList(String userId, String today, String sourceType){

    Set<Integer> probIdSet = getSolvedProbIdSet(userId, today, "", new ArrayList<>(Arrays.asList(sourceType)));

    return probIdSet.isEmpty() ? new HashSet<>()
                               : problemRepo.findSectionIdSet(probIdSet);
  }


  //21.07.01 card generator v2
  public List<Integer> getCompletedTypeIdListAfterSuppleCard(String userId, String today) {

    String lastSuppleDate = getRecentSuppleCardDate(userId, today);
    log.info("Day solved SUPPLE card recently : {}", lastSuppleDate);

    return getCompletedTypeIdList(userId, today, lastSuppleDate, CardConstants.TYPE_CARD_TYPESTR	
                                                                 + CardConstants.LRS_SOURCE_TYPE_POSTFIX);
  }
  

  public Map<String, Integer> getCompletedSectionNum(String userId, String today, String sourceType) {

    Map<String, Set<Integer>> daySolvedProbIdSet = 
        getSolvedProbIdSetByDay(userId, today, "", new ArrayList<>(Arrays.asList(sourceType)));

    Map<String, Integer> completedSectionExamCardsNum = new HashMap<>();

    for (Map.Entry<String, Set<Integer>> entry: daySolvedProbIdSet.entrySet()) {
      String sectionId = problemRepo.findSectionIdSet(entry.getValue()).iterator().next();
      Integer cardsNum = completedSectionExamCardsNum.containsKey(sectionId)
                          ? completedSectionExamCardsNum.get(sectionId) + 1
                          : 1;

      completedSectionExamCardsNum.put(sectionId, cardsNum);
    }
    
    return completedSectionExamCardsNum;
  }
}
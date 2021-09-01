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
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
import com.tmax.WaplMath.Recommend.util.config.CardConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Get schedule card solved history from LRS
 * @author Sangheon_lee
 */
@Slf4j
@Component
public class ScheduleHistoryManager {


  // Constant
  private static final Integer MAX_RECENET_STATEMENT_NUM = 200;
  private static final String ACTION_SUBMIT = "submit";

  
  @Autowired
  @Qualifier("RE-ProblemRepo")
  private ProblemRepo problemRepo;

  @Autowired
  LRSAPIManager lrsAPIManager;


  // parse numeric problem id
  public Integer parseSourceId(String sourceId){
    Integer output;

    if (sourceId == null)
      throw new RecommendException(RecommendErrorCode.NUMBER_PARSE_ERROR, sourceId);

    try {
      output = Integer.parseInt(sourceId);
    } catch (NumberFormatException e){
      throw new RecommendException(RecommendErrorCode.NUMBER_PARSE_ERROR, sourceId);
    }

    return output;
  }

  
  // 이미 푼 문제 Id set 리턴
  public Set<Integer> getSolvedProbIdSet(String userId, String tomorrow, String dateFrom, List<String> sourceTypeList){

    GetStatementInfoDTO LRSinput = GetStatementInfoDTO.builder()
                                                      .userIdList(new ArrayList<>(Arrays.asList(userId)))
                                                      .dateTo(tomorrow)
                                                      .dateFrom(!dateFrom.equals("") ? dateFrom : null)
                                                      .sourceTypeList(sourceTypeList)
                                                      .actionTypeList(new ArrayList<>(Arrays.asList(ACTION_SUBMIT)))
                                                      .recentStatementNum(MAX_RECENET_STATEMENT_NUM)
                                                      .build();

    List<StatementDTO> LRSResult = lrsAPIManager.getStatementListNew(LRSinput);

    return LRSResult.isEmpty() ? new HashSet<>()
                               : LRSResult.stream()
                                          .filter(statement -> statement.getSourceId() != null)
                                          .map(statement -> parseSourceId(statement.getSourceId()))
                                          .collect(Collectors.toSet());
  }


  // 날마다 푼 문제 Id set 리턴
  public Map<String, Set<Integer>> getSolvedProbIdSetByDay(String userId, String tomorrow, String dateFrom, 
                                                           List<String> sourceTypeList) {

    Map<String, Set<Integer>> dayProbIdSet = new HashMap<>();

    GetStatementInfoDTO LRSinput = GetStatementInfoDTO.builder()
                                                      .userIdList(new ArrayList<>(Arrays.asList(userId)))
                                                      .dateTo(tomorrow)
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

        probIdSet.add(parseSourceId(statement.getSourceId()));
        dayProbIdSet.put(date, probIdSet);
      });
    }
    return dayProbIdSet;
  }


  // 가장 최근에 푼 보충 카드 날짜 리턴 (yyyy-MM-dd)
  public String getRecentSuppleCardDate(String userId, String tomorrow) {

    GetStatementInfoDTO LRSinput = GetStatementInfoDTO.builder()
                                                      .userIdList(new ArrayList<>(Arrays.asList(userId)))
                                                      .dateTo(tomorrow)
                                                      .sourceTypeList(new ArrayList<>(Arrays.asList(CardConstants.SUPPLE_CARD_TYPESTR	+ CardConstants.LRS_SOURCE_TYPE_POSTFIX)))
                                                      .actionTypeList(new ArrayList<>(Arrays.asList(ACTION_SUBMIT)))
                                                      .recentStatementNum(1)
                                                      .build();

    List<StatementDTO> LRSResult = lrsAPIManager.getStatementListNew(LRSinput);

    return LRSResult.isEmpty() ? ""
                               : LRSResult.get(0).getTimestamp().substring(0, 10);
  }


  // 특정 카드로 푼 유형 리스트 리턴
  public List<Integer> getCompletedTypeIdList(String userId, String tomorrow, String dateFrom, String sourceType) {

    Set<Integer> probIdSet = getSolvedProbIdSet(userId, tomorrow, dateFrom, new ArrayList<>(Arrays.asList(sourceType)));

    return probIdSet.isEmpty() ? new ArrayList<>()
                               : problemRepo.findTypeIdList(probIdSet);
  }


  // 특정 카드로 푼 중단원 Id set 리턴
  public Set<String> getCompletedSectionIdList(String userId, String tomorrow, String sourceType){

    Set<Integer> probIdSet = getSolvedProbIdSet(userId, tomorrow, "", new ArrayList<>(Arrays.asList(sourceType)));

    return probIdSet.isEmpty() ? new HashSet<>()
                               : problemRepo.findSectionIdSet(probIdSet);
  }


  // (21.07.01 card generator v2) 가장 최근에 푼 보충 카드 이후에 푼 유형 카드들의 유형 리스트 리턴
  public List<Integer> getCompletedTypeIdListAfterSuppleCard(String userId, String tomorrow) {

    String lastSuppleDate = getRecentSuppleCardDate(userId, tomorrow);
    log.info("Day solved SUPPLE card recently : {}", lastSuppleDate);

    return getCompletedTypeIdList(userId, tomorrow, lastSuppleDate, CardConstants.TYPE_CARD_TYPESTR	
                                                                 + CardConstants.LRS_SOURCE_TYPE_POSTFIX);
  }
  

  // 중단원 별로 푼 카드 개수 리턴
  public Map<String, Integer> getCompletedSectionNum(String userId, String tomorrow, String sourceType) {

    Map<String, Set<Integer>> daySolvedProbIdSet = 
        getSolvedProbIdSetByDay(userId, tomorrow, "", new ArrayList<>(Arrays.asList(sourceType)));

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

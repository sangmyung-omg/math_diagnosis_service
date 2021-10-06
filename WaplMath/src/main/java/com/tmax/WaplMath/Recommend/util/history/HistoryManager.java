package com.tmax.WaplMath.Recommend.util.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Common.util.lrs.ActionType;
import com.tmax.WaplMath.Common.util.lrs.LRSManager;
import com.tmax.WaplMath.Common.util.lrs.SourceType;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
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
public class HistoryManager implements HistoryManagerInterface {

  
  @Autowired
  @Qualifier("RE-ProblemRepo")
  private ProblemRepo problemRepo;

  @Autowired
  LRSManager lrsManager;


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

  
  // 날마다 푼 문제 Id set 리턴
  public Map<String, Set<Integer>> getSolvedProbIdSetByDay(String userId, String dateFrom, String tomorrow, List<SourceType> sourceTypeList) {

    Map<String, Set<Integer>> dayProbIdSet = new HashMap<>();

    List<LRSStatementResultDTO> statementList = lrsManager.getStatementList(userId, 
                                                                            Arrays.asList(ActionType.SUBMIT), 
                                                                            sourceTypeList, dateFrom, tomorrow);

    if (!statementList.isEmpty()) {
      statementList.forEach(statement -> {

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

    List<LRSStatementResultDTO> statementList = lrsManager.getStatementList(userId, 
                                                                            Arrays.asList(ActionType.SUBMIT), 
                                                                            Arrays.asList(SourceType.SUPPLE_QUESTION), 
                                                                            "", tomorrow);

    return statementList.isEmpty() ? ""
                                   : statementList.get(0).getTimestamp().substring(0, 10);
  }


  // 이미 푼 문제 Id set 리턴
  @Override
  public Set<Integer> getSolvedProbIdSet(String userId, String dateFrom, String tomorrow, List<SourceType> sourceTypeList){

    List<LRSStatementResultDTO> statementList = lrsManager.getStatementList(userId, 
                                                                            Arrays.asList(ActionType.SUBMIT), 
                                                                            sourceTypeList, dateFrom, tomorrow);

    return statementList.isEmpty() ? new HashSet<>()
                                   : statementList.stream()
                                                  .filter(statement -> statement.getSourceId() != null)
                                                  .map(statement -> parseSourceId(statement.getSourceId()))
                                                  .collect(Collectors.toSet());
  }

  // 특정 카드로 푼 유형 리스트 리턴
  @Override
  public List<Integer> getCompletedTypeIdList(String userId, String dateFrom, String tomorrow, SourceType sourceType) {

    Set<Integer> probIdSet = getSolvedProbIdSet(userId, dateFrom, tomorrow, Arrays.asList(sourceType));

    return probIdSet.isEmpty() ? new ArrayList<>()
                               : problemRepo.findTypeIdList(probIdSet);
  }


  // 특정 카드로 푼 중단원 Id set 리턴
  @Override
  public Set<String> getCompletedSectionIdList(String userId, String dateFrom, String tomorrow, SourceType sourceType){

    Set<Integer> probIdSet = getSolvedProbIdSet(userId, dateFrom, tomorrow, Arrays.asList(sourceType));

    return probIdSet.isEmpty() ? new HashSet<>()
                               : problemRepo.findSectionIdSet(probIdSet);
  }


  // (21.07.01 card generator v2) 가장 최근에 푼 보충 카드 이후에 푼 유형 카드들의 유형 리스트 리턴
  @Override
  public List<Integer> getCompletedTypeIdListAfterSuppleCard(String userId, String tomorrow) {

    String lastSuppleDate = getRecentSuppleCardDate(userId, tomorrow);
    log.info("Day solved SUPPLE card recently : {}", lastSuppleDate);

    return getCompletedTypeIdList(userId, lastSuppleDate, tomorrow, SourceType.TYPE_QUESTION);
  }
  

  // 중단원 별로 푼 시험 대비 카드 개수 리턴
  @Override
  public Map<String, Integer> getCompletedSectionNum(String userId, String tomorrow, SourceType sourceType) {

    Map<String, Set<Integer>> daySolvedProbIdSet = 
        getSolvedProbIdSetByDay(userId, "", tomorrow, Arrays.asList(sourceType));

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

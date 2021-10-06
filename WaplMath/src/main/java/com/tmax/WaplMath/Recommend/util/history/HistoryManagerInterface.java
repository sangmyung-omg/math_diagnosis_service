package com.tmax.WaplMath.Recommend.util.history;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.tmax.WaplMath.Common.util.lrs.SourceType;

public interface HistoryManagerInterface {
  
  // 이미 푼 문제 Id set 리턴
  public Set<Integer> getSolvedProbIdSet(String userId, String dateFrom, String tomorrow, List<SourceType> sourceTypeList);
  
  // 특정 카드로 푼 유형 리스트 리턴
  public List<Integer> getCompletedTypeIdList(String userId, String dateFrom, String tomorrow, SourceType sourceType);

  // 특정 카드로 푼 중단원 Id set 리턴
  public Set<String> getCompletedSectionIdList(String userId, String dateFrom, String tomorrow, SourceType sourceType);

  // (21.07.01) 가장 최근에 푼 보충 카드 이후에 푼 유형 카드들의 유형 리스트 리턴
  public List<Integer> getCompletedTypeIdListAfterSuppleCard(String userId, String tomorrow);

  // 중단원 별로 푼 시험 대비 카드 개수 리턴
  public Map<String, Integer> getCompletedSectionNum(String userId, String tomorrow, SourceType sourceType);
}

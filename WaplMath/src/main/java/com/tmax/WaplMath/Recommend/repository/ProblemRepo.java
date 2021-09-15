package com.tmax.WaplMath.Recommend.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;
import com.tmax.WaplMath.Common.model.problem.Problem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("RE-ProblemRepo")
public interface ProblemRepo extends CrudRepository<Problem, Integer> {

  // ScheduleHistoryManager
  @Query("select distinct p.typeId from Problem p where p.probId in (:probIdSet)")
  public List<Integer> findTypeIdList(@Param("probIdSet") Set<Integer> probIdSet);

  @Query("select distinct substr(p.problemType.curriculumId, 0, 14) from Problem p where p.probId in (:probIdSet)")
  public Set<String> findSectionIdSet(@Param("probIdSet") Set<Integer> probIdSet);


  // 2021-07-01 Added by Sangheon Lee. CardGenerator
  // 2021-09-01 Modified by Sangheon Lee. Get probs modified before today
  // 2021-09-15 Modified by Sangheon Lee. For any probs category (priority)
  @Query("select count(p) from Problem p"
      + " where p.problemType.curriculumId like concat(:currId, '%')"
      + " and (coalesce(:solvedProbIdSet, null) is null or p.probId not in (:solvedProbIdSet))"
      + " and p.category in ('유형', '꼼꼼', '교과서', '기출', '모의고사')" + " and p.status = 'ACCEPT'"
      + " and (p.editDate is null or p.editDate < to_date(:today, 'yyyy-MM-dd'))"
      + " and (p.validateDate is null or p.validateDate < to_date(:today, 'yyyy-MM-dd'))")
  public Integer findProbCntInCurrId(@Param("currId") String currId, 
                                     @Param("solvedProbIdSet") Set<Integer> solvedProbIdSet, 
                                     @Param("today") String today);

  @Query("select count(p) from Problem p" + " where p.typeId=:typeId"
      + " and (coalesce(:solvedProbIdSet, null) is null or p.probId not in (:solvedProbIdSet))"
      + " and p.category in ('유형', '꼼꼼', '교과서', '기출', '모의고사')" + " and p.status = 'ACCEPT'"
      + " and (p.editDate is null or p.editDate < to_date(:today, 'yyyy-MM-dd'))"
      + " and (p.validateDate is null or p.validateDate < to_date(:today, 'yyyy-MM-dd'))")
  public Integer findProbCntInType(@Param("typeId") Integer typeId, 
                                   @Param("solvedProbIdSet") Set<Integer> solvedProbIdSet, 
                                   @Param("today") String today);

  @Query("select p from Problem p" + " where p.typeId=:typeId"
      + " and (coalesce(:solvedProbIdSet, null) is null or p.probId not in (:solvedProbIdSet))"
      + " and p.category in ('유형', '꼼꼼', '교과서', '기출', '모의고사')" + " and p.status = 'ACCEPT'"
      + " and (p.editDate is null or p.editDate < to_date(:today, 'yyyy-MM-dd'))"
      + " and (p.validateDate is null or p.validateDate < to_date(:today, 'yyyy-MM-dd'))"
      + " order by case"
        + " when p.category = '유형' and (p.frequent is null or p.frequent = 'true') then :typeFrequentPriority"
        + " when p.category = '유형' and p.frequent = 'false' then :typePriority"
        + " when p.category = '기출' then :pastPriority"
        + " when p.category = '교과서' then :pastPriority"
        + " when p.category = '모의고사' then :examPriority"
        + " when p.category = '꼼꼼' then :diagnosisPriority"
        + " else 9 end")
  public List<Problem> findProbListInTypeWithPriority(@Param("typeId") Integer typeId, 
                                                      @Param("solvedProbIdSet") Set<Integer> solvedProbIdSet, 
                                                      @Param("today") String today,
                                                      @Param("typeFrequentPriority") Integer typeFrequentPriority, 
                                                      @Param("typePriority") Integer typePriority, 
                                                      @Param("pastPriority") Integer pastPriority, 
                                                      @Param("examPriority") Integer examPriority, 
                                                      @Param("diagnosisPriority") Integer diagnosisPriority);


  // 2021-09-01 Added by Sangheon Lee. for test
  @Query("select p from Problem p where p.createDate < to_date(:today, 'yyyy-MM-dd') order by p.createDate desc")
  public List<Problem> getDateProbs(@Param("today") String today);


  // 2021-06-17 Added by Jonghyun Seong. gets Problem List from probIDList
  @Query("select p from Problem p where p.probId in :probIdList")
  public List<Problem> getProblemsByProbIdList(@Param("probIdList") List<Integer> probIdList);

}

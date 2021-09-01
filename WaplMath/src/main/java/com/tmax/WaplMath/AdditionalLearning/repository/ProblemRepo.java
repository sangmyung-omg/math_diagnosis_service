package com.tmax.WaplMath.AdditionalLearning.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.problem.Problem;


@Repository("AddLearn-ProblemRepo")
public interface ProblemRepo extends CrudRepository<UserKnowledge,String>{
	
  // 2021-09-01 Modified by Sangheon Lee. Get probs modified before today
	@Query(value="select p"+
			" from Problem p"+
			" where p.probId not in(:probIdList)"+
			" and p.problemType.curriculumId in(:curriculumIdList)"+
			" and p.status =('ACCEPT')"+
			" and (p.frequent =('true') or p.frequent is null)"+
      " and (p.editDate is null or p.editDate < to_date(:today, 'yyyy-MM-dd'))"+
      " and (p.validateDate is null or p.validateDate < to_date(:today, 'yyyy-MM-dd'))")
	List<Problem> getFrequentNotProvidedProblemByCurri(@Param("probIdList") Set<Integer> probIdList, 
																@Param("curriculumIdList") List<String> curriculumIdList,
                                @Param("today") String today);
	
	
  // 2021-09-01 Modified by Sangheon Lee. Get probs modified before today
	@Query(value="select p"+
			" from Problem p"+
			" where p.probId in(:probIdList)"+
			" and p.problemType.curriculumId in(:curriculumIdList)"+
			" and p.status =('ACCEPT')"+
			" and (p.frequent =('true') or p.frequent is null)"+
      " and (p.editDate is null or p.editDate < to_date(:today, 'yyyy-MM-dd'))"+
      " and (p.validateDate is null or p.validateDate < to_date(:today, 'yyyy-MM-dd'))")
	List<Problem> getFrequentProvidedProblemByCurri(@Param("probIdList") Set<Integer> probIdList, 
																	@Param("curriculumIdList") List<String> curriculumIdList,
                                  @Param("today") String today);
	
                                  
  // 2021-09-01 Modified by Sangheon Lee. Get probs modified before today
	@Query(value="select p"+
			" from Problem p"+
			" where p.problemType.curriculumId in(:curriculumIdList)"+
			" and p.probId not in(:probIdList)"+
			" and p.status =('ACCEPT')"+
			" and (p.frequent =('true') or p.frequent is null)"+
      " and (p.editDate is null or p.editDate < to_date(:today, 'yyyy-MM-dd'))"+
      " and (p.validateDate is null or p.validateDate < to_date(:today, 'yyyy-MM-dd'))")
	List<Problem> getFrequentAllProblemByCurri(@Param("probIdList") Set<Integer> probIdList,  
                                  @Param("curriculumIdList") List<String> curriculumIdList,
                                  @Param("today") String today);
}

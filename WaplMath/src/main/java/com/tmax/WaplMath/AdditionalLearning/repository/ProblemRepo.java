package com.tmax.WaplMath.AdditionalLearning.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.problem.Problem;


@Repository("AddLearn-ProblemRepo")
public interface ProblemRepo extends CrudRepository<UserKnowledge,String>{
	
	@Query(value="select p"+
			" from Problem p"+
			" where p.probId not in(:probIdList)"+
			" and p.problemType.curriculumId in(:curriculumIdList)"+
			" and p.frequent is not null")
	List<Problem> getFrequentNotProvidedProblem(@Param("probIdList") Set<Integer> probIdList, 
																@Param("curriculumIdList") List<String> curriculumIdList);
	
	
	
	@Query(value="select p"+
			" from Problem p"+
			" where p.probId in(:probIdList)"+
			" and p.problemType.curriculumId in(:curriculumIdList)"+
			" and p.frequent is not null")
	List<Problem> getFrequentProvidedProblem(@Param("probIdList") Set<Integer> probIdList, 
																	@Param("curriculumIdList") List<String> curriculumIdList);
}
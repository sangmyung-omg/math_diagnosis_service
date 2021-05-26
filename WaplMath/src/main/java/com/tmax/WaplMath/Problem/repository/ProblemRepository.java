package com.tmax.WaplMath.Problem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Problem.model.Problem;



public interface ProblemRepository extends CrudRepository<Problem,Integer> {

//	@Query(value="select  p.prob_id," + 
//			"p.type_id," + 
//			"p.answer_type, " + 
//			"p.LEARNING_DOMAIN, " + 
//			"p.QUESTION, " + 
//			"p.SOLUTION, " + 
//			"p.SOURCE, " + 
//			"p.CORRECT_RATE, " + 
//			"p.DIFFICULTY, " + 
//			"p.CREATOR_ID, " + 
//			"p.CREATE_DATE, " + 
//			"p.EDITOR_ID, " + 
//			"p.EDIT_DATE, " + 
//			"p.VALIDATOR_ID, " + 
//			"p.VALIDATE_DATE, " + 
//			"p.STATUS, " + 
//			"p.TIME_RECOMMENDATION, " + 
//			"p.FREQUENT, " + 
//			"p.CATEGORY, " + 
//			"i.SRC "
//			+ "from problem p , problem_image i "
//			+ "where p.prob_id=:probId "
//			+ "and i.prob_id=:probId "
//			+ "and p.prob_id = i.prob_id;",
//			nativeQuery=true)
//	List<ProblemSet> findProblemWithImage(@Param("probId") int probId);
	

	
}

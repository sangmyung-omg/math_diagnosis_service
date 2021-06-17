package com.tmax.WaplMath.AdditionalLearning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.AdditionalLearning.model.problem.UserFrequentProblem;

@Repository
public interface UserFrequentProbRepo extends CrudRepository<UserFrequentProblem,String>{
	
	
	@Query(value="select p.prob_id, c.curriculum_id"+
	" from problem p, problem_type_master t, curriculum_master c"+
	" where p.prob_id not in(:probIdList)"+
	" and c.curriculum_id in(:curriculumIdList)"+
	" and frequent is not null"+
	" and p.type_id = t.type_id and t.curriculum_id = c.curriculum_id", nativeQuery=true)
	List<UserFrequentProblem> getFrequentNotProvidedProblem(@Param("probIdList") List<Integer> probIdList, 
															@Param("curriculumIdList") List<String> curriculumIdList);
	//List<UserFrequentProblem> getFrequentProvidedProblem(@Param("userId") String userId, @Param("probIdList") List<String> probIdList);
	
	
	/*
	@Query(value="select p.prob_id, c.curriculum_id"+
			" from problem p, problem_uk_rel r, uk_master u,user_knowledge k, curriculum_master c"+
			" where user_uuid = :userId"+
			" and p.prob_id not in(:probIdList)"+
			" and c.curriculum_id in(:curriculumIdList)"+
			" and frequent is not null"+
			" and p.prob_id = r.prob_id and r.uk_id = u.uk_id and u.uk_id = k.uk_id and u.curriculum_id = c.curriculum_id"+
			" group by p.prob_id,c.curriculum_id", nativeQuery=true)
			List<UserFrequentProblem> getFrequentNotProvidedProblem(@Param("userId") String userId, 
																	@Param("probIdList") List<Integer> probIdList, 
																	@Param("curriculumIdList") List<String> curriculumIdList);
	*/

	
}

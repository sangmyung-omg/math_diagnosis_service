package com.tmax.WaplMath.AdditionalLearning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.AdditionalLearning.model.problem.UserSubSectionMastery;

public interface UserSubSectionMasteryRepo extends CrudRepository<UserSubSectionMastery,String>{
	
	@Query(value="select avg(uk_mastery) uk_mastery, c.curriculum_id"+
			" from problem p, problem_type_master t, type_uk_rel r, user_knowledge k, curriculum_master c"+
			" where user_uuid = :userId"+
			" and p.prob_id in(:probIdList)"+
			" and p.type_id = t.type_id and t.type_id = r.type_id and r.uk_id = k.uk_id and t.curriculum_id = c.curriculum_id"+
			" group by c.curriculum_id"+
			" order by uk_mastery asc", nativeQuery=true)
	List<UserSubSectionMastery> getSubSectionAndMastery(@Param("userId") String userId, @Param("probIdList") List<Integer> probIdList);
	

	/*
	@Query(value="select avg(uk_mastery) uk_mastery, c.curriculum_id"+
			" from problem p, problem_uk_rel r, uk_master u,user_knowledge k, curriculum_master c"+
			" where user_uuid = :userId"+
			" and p.prob_id in(:probIdList)"+
			" and p.prob_id = r.prob_id and r.uk_id = u.uk_id and u.uk_id = k.uk_id and u.curriculum_id = c.curriculum_id"+
			" group by c.curriculum_id", nativeQuery=true)
	List<UserSubSectionMastery> getSubSectionAndMastery(@Param("userId") String userId, @Param("probIdList") List<Integer> probIdList);
	*/
}

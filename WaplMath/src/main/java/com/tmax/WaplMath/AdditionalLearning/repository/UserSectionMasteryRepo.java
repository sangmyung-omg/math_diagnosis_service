package com.tmax.WaplMath.AdditionalLearning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.AdditionalLearning.model.problem.UserSectionMastery;

public interface UserSectionMasteryRepo extends CrudRepository<UserSectionMastery,String>{
	
	@Query(value="select avg(uk_mastery) uk_mastery, c.sub_section"+
			" from problem p, problem_type_master t, type_uk_rel r, user_knowledge k, curriculum_master c"+
			" where user_uuid = :userId"+
			" and p.prob_id in(:probIdList)"+
			" and p.type_id = t.type_id and t.type_id = r.type_id and r.uk_id = k.uk_id and t.curriculum_id = c.curriculum_id"+
			" group by c.sub_section", nativeQuery=true)
	List<UserSectionMastery> getSubSectionAndMastery(@Param("userId") String userId, @Param("probIdList") List<Integer> probIdList);

}

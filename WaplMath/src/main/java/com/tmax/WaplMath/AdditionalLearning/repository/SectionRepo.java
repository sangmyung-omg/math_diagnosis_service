package com.tmax.WaplMath.AdditionalLearning.repository;

import java.util.List;
import java.util.Set;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository("AddLearn-SectionRepo")
public interface SectionRepo extends CrudRepository<UserKnowledge,String>{
	@Query(value="select k.uk.curriculumId, avg(k.ukMastery) as ukMastery"+
			" from Problem p inner join TypeUkRel r on p.typeId = r.typeId inner join UserKnowledge k on r.ukId = k.ukId"+
			" where k.userUuid = :userId"+
			" and p.probId in(:probIdList)"+
			" and k.uk.curriculumId >= (:scopeStart)"+
			" and k.uk.curriculumId <= (:scopeEnd)"+
			" group by k.uk.curriculumId"+
			" order by ukMastery")
	List<String> getCurriculumAndMasteryByProbId(@Param("userId") String userId, @Param("probIdList") Set<Integer> probIdList, @Param("scopeStart") String scopeStart, @Param("scopeEnd") String scopeEnd);
}

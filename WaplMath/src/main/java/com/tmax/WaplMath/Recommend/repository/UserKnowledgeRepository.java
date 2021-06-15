package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledgeKey;

public interface UserKnowledgeRepository extends CrudRepository<UserKnowledge, UserKnowledgeKey> {

//	@Query("SELECT ukd FROM UserKnowledgeDAO ukd INNER JOIN UkDAO ud ON ukd.ukUuid = ud.ukUuid WHERE ukd.userUuid = ?1 AND SUBSTR(ud.curriculumId, 1, 11) = ?2 ORDER BY ud.curriculumId asc")
	@Query("SELECT ukd FROM UserKnowledge ukd WHERE ukd.userUuid = ?1 AND SUBSTR(ukd.uk.curriculumId, 1, 11) = ?2 ORDER BY ukd.uk.curriculumId asc")
	List<UserKnowledge> findAllByUserAndChapter(String user_uuid, String curriculum_id);

	@Query("select ukd.ukMastery from UserKnowledge ukd, ProblemUkRel pukr where ukd.userUuid = :userId and pukr.probId = :probId and ukd.ukId = pukr.ukId")
	public List<Float> findAllMasteryByProbId(@Param("userId") String userId, @Param("probId") Integer probId);

	@Query("select ukd from UserKnowledge ukd where ukd.userUuid = :userId and ukd.ukId in (:solvedUkIdList) and ukd.ukId not in (:suppleUkIdList) and ukd.ukMastery > :threshold")
	List<UserKnowledge> findAllLowMasteryUkUuid(@Param("userId") String userId,
			@Param("solvedUkIdList") List<Integer> solvedUkIdList,
			@Param("suppleUkIdList") List<Integer> suppleCardUkList, @Param("threshold") Float SUP_UK_MASTERY_THRESHOLD);

	@Query("select pt.typeId from ProblemType pt")
	List<Integer> findAllLowMasteryTypeId(@Param("userId") String userId,
			@Param("solvedTypeIdList") List<Integer> solvedTypeIdList,
			@Param("suppleTypeIdList") List<Integer> suppleTypeIdList, @Param("threshold") Float LOW_MASTERY_THRESHOLD);
}

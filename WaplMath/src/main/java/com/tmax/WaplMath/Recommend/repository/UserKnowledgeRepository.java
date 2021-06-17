package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.dto.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledgeKey;

public interface UserKnowledgeRepository extends CrudRepository<UserKnowledge, UserKnowledgeKey> {

	//	@Query("SELECT ukd FROM UserKnowledgeDAO ukd INNER JOIN UkDAO ud ON ukd.ukUuid = ud.ukUuid WHERE ukd.userUuid = ?1 AND SUBSTR(ud.curriculumId, 1, 11) = ?2 ORDER BY ud.curriculumId asc")
	@Query("SELECT ukd FROM UserKnowledge ukd WHERE ukd.userUuid = ?1 AND SUBSTR(ukd.uk.curriculumId, 1, 11) = ?2 ORDER BY ukd.uk.curriculumId asc")
	List<UserKnowledge> findAllByUserAndChapter(String user_uuid, String curriculum_id);

	@Query("select ukd.ukMastery from UserKnowledge ukd, ProblemUkRel pukr where ukd.userUuid = :userId and pukr.probId = :probId and ukd.ukId = pukr.ukId")
	public List<Float> findAllMasteryByProbId(@Param("userId") String userId, @Param("probId") Integer probId);

	@Query("select ukd from UserKnowledge ukd where ukd.userUuid = :userId and ukd.ukId in (:solvedUkIdList) and ukd.ukId not in (:suppleUkIdList) and ukd.ukMastery > :threshold")
	List<UserKnowledge> findAllLowMasteryUkUuid(@Param("userId") String userId, @Param("solvedUkIdList") List<Integer> solvedUkIdList,
			@Param("suppleUkIdList") List<Integer> suppleCardUkList, @Param("threshold") Float SUP_UK_MASTERY_THRESHOLD);

	//신
	@Query(value = "select typeId, mastery from " + "( select avg(ukl.uk_mastery) mastery, tur.type_id typeId "
			+ "from user_knowledge ukl, uk_master um, type_uk_rel tur " + "where um.uk_id = tur.uk_id " + "and ukl.uk_id = um.uk_id "
			+ "and ukl.user_uuid = :userId " + "group by tur.type_id ) "
			+ "where (coalesce(:solvedTypeIdList, null) is null or typeId in (:solvedTypeIdList)) "
			+ "and (coalesce(:suppleTypeIdList, null) is null or typeId not in (:suppleTypeIdList)) "
			+ "and mastery >= :threshold order by mastery asc", nativeQuery = true)
	List<TypeMasteryDTO> findAllLowMasteryTypeId(@Param("userId") String userId, @Param("solvedTypeIdList") List<Integer> solvedTypeIdList,
			@Param("suppleTypeIdList") List<Integer> suppleTypeIdList, @Param("threshold") Float LOW_MASTERY_THRESHOLD);
}

package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.dto.SectionMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.SubSectionMasteryDTO;
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
		+ "and mastery <= :threshold order by mastery asc", nativeQuery = true)
	List<TypeMasteryDTO> findLowTypeMasteryList(@Param("userId") String userId, @Param("solvedTypeIdList") List<Integer> solvedTypeIdList,
		@Param("suppleTypeIdList") List<Integer> suppleTypeIdList, @Param("threshold") Float LOW_MASTERY_THRESHOLD);

	@Query("select tur.typeId as typeId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl, TypeUkRel tur where ukl.userUuid = :userId and tur.ukId = ukl.ukId and tur.typeId = :typeId group by tur.typeId")
	TypeMasteryDTO findTypeMastery(@Param("userId") String userId, @Param("typeId") Integer typeId);

	@Query("select tur.typeId as typeId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl, TypeUkRel tur where ukl.userUuid = :userId and tur.ukId = ukl.ukId and (coalesce(:typeIdList, null) is null or tur.typeId in (:typeIdList)) group by tur.typeId order by mastery asc")
	List<TypeMasteryDTO> findTypeMasteryList(@Param("userId") String userId, @Param("typeIdList") List<Integer> typeIdList);

	@Query("select ukl.uk.curriculumId as subSectionId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdList, null) is null or ukl.uk.curriculumId in (:subSectionIdList)) group by ukl.uk.curriculumId order by mastery asc")
	List<SubSectionMasteryDTO> findSubSectionMasteryList(@Param("userId") String userId, @Param("subSectionIdList") List<String> subSectionIdList);

	@Query("select substr(ukl.uk.curriculumId, 1, 14) as sectionId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId like concat(:sectionId, '%') group by substr(ukl.uk.curriculumId, 1, 14)")
	SectionMasteryDTO findSectionMastery(@Param("userId") String userId, @Param("sectionId") String sectionId);

}

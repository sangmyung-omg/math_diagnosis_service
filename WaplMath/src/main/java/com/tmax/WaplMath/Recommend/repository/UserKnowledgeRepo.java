package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledgeKey;
import com.tmax.WaplMath.Recommend.dto.mastery.CurrMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;

@Repository("RE-UserKnowledgeRepo")
public interface UserKnowledgeRepo extends CrudRepository<UserKnowledge, UserKnowledgeKey> {

  // 21.07.21 find by userId
  @Query("select distinct know.userUuid from UserKnowledge know")
  public Set<String> findExistUserList();
  
	//	@Query("SELECT ukd FROM UserKnowledgeDAO ukd INNER JOIN UkDAO ud ON ukd.ukUuid = ud.ukUuid WHERE ukd.userUuid = ?1 AND SUBSTR(ud.curriculumId, 1, 11) = ?2 ORDER BY ud.curriculumId asc")
	@Query("SELECT ukd FROM UserKnowledge ukd WHERE ukd.userUuid = ?1 AND SUBSTR(ukd.uk.curriculumId, 1, 11) = ?2 ORDER BY ukd.uk.curriculumId asc")
	List<UserKnowledge> findAllByUserAndChapter(String user_uuid, String curriculum_id);

	@Query("select ukd.ukMastery from UserKnowledge ukd, ProblemUkRel pukr where ukd.userUuid = :userId and pukr.probId = :probId and ukd.ukId = pukr.ukId")
	public List<Float> findAllMasteryByProbId(@Param("userId") String userId, @Param("probId") Integer probId);

	@Query("select ukd from UserKnowledge ukd where ukd.userUuid = :userId and ukd.ukId in (:solvedUkIdList) and ukd.ukId not in (:suppleUkIdList) and ukd.ukMastery > :threshold")
	List<UserKnowledge> findAllLowMasteryUkUuid(@Param("userId") String userId, @Param("solvedUkIdList") List<Integer> solvedUkIdList,
		@Param("suppleUkIdList") List<Integer> suppleCardUkList, @Param("threshold") Float SUP_UK_MASTERY_THRESHOLD);

	//v1
	@Query(value = "select typeId, mastery from " + "( select avg(ukl.uk_mastery) mastery, tur.type_id typeId "
		+ "from user_knowledge ukl, uk_master um, type_uk_rel tur " + "where um.uk_id = tur.uk_id " + "and ukl.uk_id = um.uk_id "
		+ "and ukl.user_uuid = :userId " + "group by tur.type_id ) "
		+ "where (coalesce(:solvedTypeIdList, null) is null or typeId in (:solvedTypeIdList)) "
		+ "and (coalesce(:suppleTypeIdList, null) is null or typeId not in (:suppleTypeIdList)) "
		+ "and mastery <= :threshold order by mastery asc", nativeQuery = true)
	List<TypeMasteryDTO> findLowTypeMasteryList(@Param("userId") String userId, @Param("solvedTypeIdList") List<Integer> solvedTypeIdList, @Param("suppleTypeIdList") List<Integer> suppleTypeIdList, @Param("threshold") Float LOW_MASTERY_THRESHOLD);

	@Query("select tur.typeId as typeId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl, TypeUkRel tur where ukl.userUuid = :userId and tur.ukId = ukl.ukId and tur.typeId = :typeId group by tur.typeId")
	TypeMasteryDTO findTypeMastery(@Param("userId") String userId, @Param("typeId") Integer typeId);

	@Query("select ukl.uk.curriculumId as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.subSection as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdList, null) is null or ukl.uk.curriculumId in (:subSectionIdList)) group by ukl.uk.curriculumId, ukl.uk.curriculum.subSection order by mastery asc")
	List<CurrMasteryDTO> findSubSectionMasteryList(@Param("userId") String userId, @Param("subSectionIdList") List<String> subSectionIdList);

	@Query("select substr(ukl.uk.curriculumId, 1, 14) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.section as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:sectionIdList, null) is null or substr(ukl.uk.curriculumId, 1, 14) in (:sectionIdList)) group by substr(ukl.uk.curriculumId, 1, 14), ukl.uk.curriculum.section order by mastery asc")
	List<CurrMasteryDTO> findSectionMasteryList(@Param("userId") String userId, @Param("sectionIdList") List<String> sectionIdList);

	@Query("select substr(ukl.uk.curriculumId, 1, 11) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.chapter as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:chapterIdList, null) is null or substr(ukl.uk.curriculumId, 1, 11) in (:chapterIdList)) group by substr(ukl.uk.curriculumId, 1, 11), ukl.uk.curriculum.chapter order by mastery asc")
	List<CurrMasteryDTO> findChapterMasteryList(@Param("userId") String userId, @Param("chapterIdList") List<String> chapterIdList);

	@Query("select substr(ukl.uk.curriculumId, 1, 14) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.section as currName from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId like concat(:sectionId, '%') group by substr(ukl.uk.curriculumId, 1, 14), ukl.uk.curriculum.section")
	CurrMasteryDTO findSectionMastery(@Param("userId") String userId, @Param("sectionId") String sectionId);

	@Query("select substr(ukl.uk.curriculumId, 1, 11) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.chapter as currName from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId like concat(:chapterId, '%') group by substr(ukl.uk.curriculumId, 1, 11), ukl.uk.curriculum.chapter")
	CurrMasteryDTO findChapterMastery(@Param("userId") String userId, @Param("chapterId") String chapterId);

	@Query("select avg(ukl.ukMastery) as mastery from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdSet, null) is null or ukl.uk.curriculumId in (:subSectionIdSet))")
	CurrMasteryDTO findExamMastery(@Param("userId") String userId, @Param("subSectionIdSet") Set<String> subSectionIdSet);


	//21.07.01 card generator v2
	@Query("select ukl.uk.curriculumId as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.subSection as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdSet, null) is null or ukl.uk.curriculumId in (:subSectionIdSet)) and ukl.uk.curriculumId like concat(:sectionId, '%') group by ukl.uk.curriculumId, ukl.uk.curriculum.subSection order by mastery asc")
	List<CurrMasteryDTO> findMasteryListInSectionOrderByMastery(@Param("userId") String userId, @Param("sectionId") String sectionId,
  @Param("subSectionIdSet") Set<String> subSectionIdSet);

	@Query("select substr(ukl.uk.curriculumId, 1, 14) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.section as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdSet, null) is null or ukl.uk.curriculumId in (:subSectionIdSet)) and ukl.uk.curriculumId like concat(:chapterId, '%') group by substr(ukl.uk.curriculumId, 1, 14), ukl.uk.curriculum.section order by mastery asc")
	List<CurrMasteryDTO> findMasteryListInChapterOrderByMastery(@Param("userId") String userId, @Param("chapterId") String chapterId,
  @Param("subSectionIdSet") Set<String> subSectionIdSet);

	@Query("select substr(ukl.uk.curriculumId, 1, 11) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.chapter as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdSet, null) is null or ukl.uk.curriculumId in (:subSectionIdSet)) group by substr(ukl.uk.curriculumId, 1, 11), ukl.uk.curriculum.chapter order by mastery asc")
	List<CurrMasteryDTO> findMasteryListInSubSectionSetOrderByMastery(@Param("userId") String userId,
		@Param("subSectionIdSet") Set<String> subSectionIdSet);


	@Query("select ukl.uk.curriculumId as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.subSection as currName from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId =:subSectionId group by ukl.uk.curriculumId, ukl.uk.curriculum.subSection")
	CurrMasteryDTO findMasteryBySubSection(@Param("userId") String userId, @Param("subSectionId") String subSectionId);

	@Query("select substr(ukl.uk.curriculumId, 1, 14) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.section as currName from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId like concat(:sectionId, '%') group by substr(ukl.uk.curriculumId, 1, 14), ukl.uk.curriculum.section")
	CurrMasteryDTO findMasteryBySection(@Param("userId") String userId, @Param("sectionId") String sectionId);

	@Query("select substr(ukl.uk.curriculumId, 1, 11) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.chapter as currName from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId like concat(:chapterId, '%') group by substr(ukl.uk.curriculumId, 1, 11), ukl.uk.curriculum.chapter")
	CurrMasteryDTO findMasteryByChapter(@Param("userId") String userId, @Param("chapterId") String chapterId);


	@Query(value = "select typeId, mastery from " + "( select avg(ukl.uk_mastery) mastery, tur.type_id typeId "
		+ "from user_knowledge ukl, uk_master um, type_uk_rel tur " + "where um.uk_id = tur.uk_id " + "and ukl.uk_id = um.uk_id "
		+ "and ukl.user_uuid = :userId " + "group by tur.type_id ) " + "where (coalesce(:typeIdList, null) is null or typeId in (:typeIdList)) "
		+ "and mastery <= :threshold order by mastery asc", nativeQuery = true)
	List<TypeMasteryDTO> findNLowTypeMasteryList(@Param("userId") String userId, @Param("typeIdList") List<Integer> typeIdList, @Param("threshold") Float LOW_MASTERY_THRESHOLD);

	
	@Query("select tur.typeId as typeId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl, TypeUkRel tur where ukl.userUuid = :userId and tur.ukId = ukl.ukId and (coalesce(:typeIdList, null) is null or tur.typeId in (:typeIdList)) group by tur.typeId order by mastery asc")
	List<TypeMasteryDTO> findTypeMasteryList(@Param("userId") String userId, @Param("typeIdList") List<Integer> typeIdList);


	@Query("select tur.typeId as typeId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl, TypeUkRel tur, Curriculum scm, Curriculum ecm "
		+ "where ukl.userUuid = :userId and ukl.ukId = tur.ukId "
		+ "and scm.curriculumId = :startSubSectionId and tur.problemType.curriculum.curriculumSequence >= scm.curriculumSequence "
		+ "and ecm.curriculumId = :endSubSectionId and tur.problemType.curriculum.curriculumSequence <= ecm.curriculumSequence "
		+ "group by tur.typeId order by mastery")
	List<TypeMasteryDTO> findTypeMasteryListBetween(@Param("userId") String userId, @Param("startSubSectionId") String startSubSectionId, @Param("endSubSectionId") String endSubSectionId);


}

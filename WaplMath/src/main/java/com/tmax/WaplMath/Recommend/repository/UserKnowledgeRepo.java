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

  // 21.07.21 find by userId - ScheduleServiceV2
  @Query("select distinct know.userUuid from UserKnowledge know")
  public Set<String> findExistUserList();
  

  // CardGenerator
	@Query("select tur.typeId as typeId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl, TypeUkRel tur where ukl.userUuid = :userId and tur.ukId = ukl.ukId and tur.typeId = :typeId group by tur.typeId")
	TypeMasteryDTO findTypeMastery(@Param("userId") String userId, @Param("typeId") Integer typeId);

	@Query("select ukl.uk.curriculumId as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.subSection as currName from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId =:subSectionId group by ukl.uk.curriculumId, ukl.uk.curriculum.subSection")
	CurrMasteryDTO findSubSectionMastery(@Param("userId") String userId, @Param("subSectionId") String subSectionId);

	@Query("select substr(ukl.uk.curriculumId, 1, 14) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.section as currName from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId like concat(:sectionId, '%') group by substr(ukl.uk.curriculumId, 1, 14), ukl.uk.curriculum.section")
	CurrMasteryDTO findSectionMastery(@Param("userId") String userId, @Param("sectionId") String sectionId);

	@Query("select substr(ukl.uk.curriculumId, 1, 11) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.chapter as currName from UserKnowledge ukl where ukl.userUuid = :userId and ukl.uk.curriculumId like concat(:chapterId, '%') group by substr(ukl.uk.curriculumId, 1, 11), ukl.uk.curriculum.chapter")
	CurrMasteryDTO findChapterMastery(@Param("userId") String userId, @Param("chapterId") String chapterId);
  
	@Query("select avg(ukl.ukMastery) as mastery from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdSet, null) is null or ukl.uk.curriculumId in (:subSectionIdSet))")
	CurrMasteryDTO findExamMastery(@Param("userId") String userId, @Param("subSectionIdSet") Set<String> subSectionIdSet);


	//21.07.01 CardGenerator v2
	@Query("select ukl.uk.curriculumId as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.subSection as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdSet, null) is null or ukl.uk.curriculumId in (:subSectionIdSet)) and ukl.uk.curriculumId like concat(:sectionId, '%') group by ukl.uk.curriculumId, ukl.uk.curriculum.subSection order by mastery asc")
	List<CurrMasteryDTO> findMasteryListInSectionOrderByMastery(@Param("userId") String userId, @Param("sectionId") String sectionId,
  @Param("subSectionIdSet") Set<String> subSectionIdSet);

	@Query("select substr(ukl.uk.curriculumId, 1, 14) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.section as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdSet, null) is null or ukl.uk.curriculumId in (:subSectionIdSet)) and ukl.uk.curriculumId like concat(:chapterId, '%') group by substr(ukl.uk.curriculumId, 1, 14), ukl.uk.curriculum.section order by mastery asc")
	List<CurrMasteryDTO> findMasteryListInChapterOrderByMastery(@Param("userId") String userId, @Param("chapterId") String chapterId,
  @Param("subSectionIdSet") Set<String> subSectionIdSet);

	@Query("select substr(ukl.uk.curriculumId, 1, 11) as currId, avg(ukl.ukMastery) as mastery, ukl.uk.curriculum.chapter as currName from UserKnowledge ukl where ukl.userUuid = :userId and (coalesce(:subSectionIdSet, null) is null or ukl.uk.curriculumId in (:subSectionIdSet)) group by substr(ukl.uk.curriculumId, 1, 11), ukl.uk.curriculum.chapter order by mastery asc")
	List<CurrMasteryDTO> findMasteryListInSubSectionSetOrderByMastery(@Param("userId") String userId,
		@Param("subSectionIdSet") Set<String> subSectionIdSet);
	

  // ScheduleConfigurator
	@Query(value = "select typeId, mastery from " 
    + "( select avg(ukl.uk_mastery) mastery, tur.type_id typeId "
      + "from user_knowledge ukl, uk_master um, type_uk_rel tur " 
      + "where um.uk_id = tur.uk_id " + "and ukl.uk_id = um.uk_id "
		  + "and ukl.user_uuid = :userId " + "group by tur.type_id ) " 
    + "where (coalesce(:typeIdList, null) is null or typeId in (:typeIdList)) "
		+ "and mastery <= :threshold order by mastery asc", nativeQuery = true)
	List<TypeMasteryDTO> findLowTypeMasteryList(@Param("userId") String userId, @Param("typeIdList") List<Integer> typeIdList, @Param("threshold") Float LOW_MASTERY_THRESHOLD);
	
	@Query("select tur.typeId as typeId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl, TypeUkRel tur where ukl.userUuid = :userId and tur.ukId = ukl.ukId and (coalesce(:typeIdList, null) is null or tur.typeId in (:typeIdList)) group by tur.typeId order by mastery asc")
	List<TypeMasteryDTO> findTypeMasteryList(@Param("userId") String userId, @Param("typeIdList") List<Integer> typeIdList);

	@Query("select tur.typeId as typeId, avg(ukl.ukMastery) as mastery from UserKnowledge ukl, TypeUkRel tur, Curriculum scm, Curriculum ecm "
		+ "where ukl.userUuid = :userId and ukl.ukId = tur.ukId "
		+ "and scm.curriculumId = :startSubSectionId and tur.problemType.curriculum.curriculumSequence >= scm.curriculumSequence "
		+ "and ecm.curriculumId = :endSubSectionId and tur.problemType.curriculum.curriculumSequence <= ecm.curriculumSequence "
		+ "group by tur.typeId order by mastery")
	List<TypeMasteryDTO> findTypeMasteryListBetween(@Param("userId") String userId, @Param("startSubSectionId") String startSubSectionId, @Param("endSubSectionId") String endSubSectionId);


}

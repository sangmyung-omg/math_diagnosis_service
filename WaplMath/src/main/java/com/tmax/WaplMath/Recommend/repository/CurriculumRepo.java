package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import java.util.Set;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("RE-CurriculumRepo")
public interface CurriculumRepo extends CrudRepository<Curriculum, String> {

	@Query("SELECT DISTINCT C.part FROM Curriculum C WHERE C.schoolType='중등'")
	List<String> findDistinctPart();

	@Query("SELECT C FROM Curriculum C WHERE C.part = ?1 AND CHAR_LENGTH(C.curriculumId)=11 AND C.schoolType='중등' ORDER BY C.curriculumId ASC")
	List<Curriculum> findChaptersByPart(String part);

	@Query("SELECT C FROM Curriculum C WHERE C.part = ?1 AND ((CHAR_LENGTH(C.curriculumId)=11 AND C.schoolType='중등') OR (C.schoolType='초등')) ORDER BY C.curriculumSequence ASC")
	List<Curriculum> findChaptersByPartIncludingElementary(String part);

	@Query("SELECT C FROM Curriculum C WHERE C.part = ?1 AND ((CHAR_LENGTH(C.curriculumId)=14 AND C.schoolType='중등') OR (C.schoolType='초등')) ORDER BY C.curriculumSequence ASC")
	List<Curriculum> findSectionsByPartIncludingElementary(String part);

	@Query("SELECT DISTINCT CM.chapter FROM Curriculum CM WHERE CM.curriculumId LIKE ?1")
	List<String> findAllByCurriculumIdLike(String curriculumId);

	@Query("SELECT CM FROM Curriculum CM WHERE CM.grade = ?1 AND CM.chapter = ?2 AND CHAR_LENGTH(CM.curriculumId)=11")
	Curriculum findByChapter(String grade, String chapter);

	@Query("select cm.curriculumId from Curriculum cm where cm.curriculumSequence >= :startSeq and cm.curriculumSequence <= :endSeq and CHAR_LENGTH(cm.curriculumId)=17")
	List<String> findSubSectionListBySeq(@Param("startSeq") Integer startSeq, @Param("endSeq") Integer endSeq);

	//v1
	@Query("select cm.curriculumId from Curriculum cm, Curriculum scm, Curriculum ecm where scm.curriculumId = :startSubSectionId and ecm.curriculumId = :endSubSectionId and cm.curriculumSequence >= scm.curriculumSequence and cm.curriculumSequence <= ecm.curriculumSequence and cm.subSection is not null order by cm.curriculumSequence asc")
	List<String> findSubSectionListBetween(@Param("startSubSectionId") String startSubSectionId, 
                                         @Param("endSubSectionId") String endSubSectionId);

	@Query("select cm.curriculumId from Curriculum cm, Curriculum scm, Curriculum ecm where scm.curriculumId = :startSubSectionId and ecm.curriculumId = :endSubSectionId and cm.curriculumSequence >= scm.curriculumSequence and cm.curriculumSequence <= ecm.curriculumSequence and cm.subSection is not null and (coalesce(:exceptSubSectionIdList, null) is null or cm.curriculumId not in (:exceptSubSectionIdList))")
	List<String> findSubSectionListBetweenExcept(@Param("startSubSectionId") String startSubSectionId, 
                                               @Param("endSubSectionId") String endSubSectionId, 
                                               @Param("exceptSubSectionIdList") List<String> exceptSubSectionIdList);

	@Query("select distinct cm.curriculumId from Curriculum cm where cm.curriculumId like concat(:sectionId, '%') and cm.subSection is not null")
	List<String> findSubSectionListInSection(@Param("sectionId") String sectionId);
	
	@Query("select distinct cm.curriculumId from Curriculum cm where cm.curriculumId like concat(:chapterId, '%') and cm.subSection is null and cm.section is not null")
	List<String> findSectionListInChapter(@Param("chapterId") String chapterId);
	
	@Query("select distinct substr(cm.curriculumId, 1, 11) from Curriculum cm where cm.curriculumId in (:subSectionIdSet)")
	List<String> findChapterListInSubSectionSet(@Param("subSectionIdSet") Set<String> subSectionIdSet);

	@Query("select cm.subSection from Curriculum cm where cm.curriculumId=:subSectionId")
	String findSubSectionName(@Param("subSectionId") String subSectionId);

	@Query("select cm.section from Curriculum cm where cm.curriculumId=:sectionId")
	String findSectionName(@Param("sectionId") String sectionId);
	
	@Query("select cm.chapter from Curriculum cm where cm.curriculumId=:chapterId")
	String findChapterName(@Param("chapterId") String chapterId);
	
	@Query("select pt.curriculum from ProblemType pt where pt.typeId = :typeId")
	Curriculum findByType(@Param("typeId")Integer typeId);
	
	//21.07.01 card generator v2
	@Query("select cm.curriculumId from Curriculum cm where (coalesce(:currIdSet, null) is null or cm.curriculumId in (:currIdSet)) order by cm.curriculumSequence asc")
	List<String> sortByCurrSeq(@Param("currIdSet") Set<String> currIdSet);

	//21.07.05 exam card configurator v2
	@Query("select cm.curriculumId from Curriculum cm where (coalesce(:sectionIdSet, null) is null or substr(cm.curriculumId, 1, 14) in (:sectionIdSet)) and cm.subSection is not null order by cm.curriculumSequence asc")
	List<String> findSubSectionListInSectionSet(@Param("sectionIdSet") Set<String> sectionIdSet);
	
	// 21.06.22. extra problems
	@Query("SELECT DISTINCT C.part FROM Curriculum C, Curriculum SC, Curriculum EC WHERE SC.curriculumId = ?1 AND EC.curriculumId = ?2 AND C.curriculumSequence >= SC.curriculumSequence AND C.curriculumSequence <= EC.curriculumSequence AND C.subSection IS NOT NULL")
	List<String> findDistinctPartBetween(String startSubSectionId, String endSubSectionId);
}
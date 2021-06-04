package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;

public interface CurriculumRepository extends CrudRepository<Curriculum, String> {
	
	@Query("SELECT DISTINCT C.part FROM Curriculum C WHERE C.schoolType='중등'")
	List<String> findDistinctPart();
	
	@Query("SELECT C FROM Curriculum C WHERE C.part = ?1 AND CHAR_LENGTH(C.curriculumId)=11 AND C.schoolType='중등' ORDER BY C.curriculumId ASC")
	List<Curriculum> findChaptersByPart(String part);
	// DB에 part 값이 없음.
	
	@Query("SELECT DISTINCT CM.chapter FROM Curriculum CM WHERE CM.curriculumId LIKE ?1")
	List<String> findAllByCurriculumIdLike(String curriculumId);

	@Query("SELECT CM FROM Curriculum CM WHERE CM.grade = ?1 AND CM.chapter = ?2 AND CHAR_LENGTH(CM.curriculumId)=11")
	Curriculum findByChapter(String grade, String chapter);	
	
	@Query("select cm.section from Curriculum cm where cm.curriculumId=:curriculumId")
	String findSectionName(@Param("curriculumId") String curriculum_id);
	
	@Query("select cm.curriculumId from Curriculum cm where cm.curriculumSequence >= :startSeq and cm.curriculumSequence <= :endSeq and CHAR_LENGTH(cm.curriculumId)=17")
	List<String> findExamSubSectionList(@Param("startSeq") Integer startSeq, @Param("endSeq") Integer endSeq);
}
package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.Curriculum;

public interface CurriculumRepository extends CrudRepository<Curriculum, String> {
	
	@Query("SELECT C FROM Curriculum C WHERE C.part = ?1 AND CHAR_LENGTH(C.curriculumId)=11 AND C.schoolType='중등' ORDER BY C.curriculumId")
	List<Curriculum> findChaptersByParts(String part);
	
	@Query("SELECT DISTINCT CM.chapter FROM Curriculum CM WHERE CM.curriculumId LIKE ?1")
	List<String> findAllByCurriculumIdLike(String curriculumId);

	@Query("SELECT CM FROM Curriculum CM WHERE CM.grade = ?1 AND CM.chapter = ?2 AND CHAR_LENGTH(CM.curriculumId)=11")
	Curriculum findByChapter(String grade, String chapter);

	@Query(value="SELECT * "
			+ "FROM "
				+ "curriculum_master a, " 
				+ "(select curriculum_sequence from curriculum_master where curriculum_id=:chapter) b " 
			+ "where "
				+ "CHAR_LENGTH(a.curriculum_id)=14 "
			+ "and "
				+ "a.curriculum_sequence < b.curriculum_sequence "
			+ "order by "
				+ "a.curriculum_sequence "
			+ "DESC limit 1", nativeQuery=true)
	Curriculum findNearestSection(@Param("chapter") String chapter);
	
	@Query(value="select section from curriculum_master where curriculum_id=?1", nativeQuery=true)
	String findSectionNameById(String curriculum_id);
}
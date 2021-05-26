package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.Problem;

public interface ProblemRepository extends CrudRepository<Problem, Integer>{
	
	Problem findByProbId(String ProbId);
	
//	@Query("select distinct P.ukUuid from ProblemDemo P where substr(P.chapter,1,14)=:sectionId")
//	List<String> findAllUkUuidBySection(@Param("sectionId") String sectionId);
//	
//	@Query("select distinct P.ukUuid from ProblemDemo P where P.probTypeUuid=:typeUkId")
//	List<String> findAllUkUuidByTypeUkId(@Param("typeUkId") String typeUkId);
//
//	@Query("select distinct P.ukUuid from ProblemDemo P where substr(P.chapter,1,11) in :chapterList")
//	List<String> findAllUkUuidByChapterList(@Param("chapterList") List<String> chapterList);
}

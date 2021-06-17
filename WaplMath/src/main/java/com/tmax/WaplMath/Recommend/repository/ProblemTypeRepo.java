package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.problem.ProblemType;

public interface ProblemTypeRepo extends CrudRepository<ProblemType, Integer> {


	@Query("select substr(pt.curriculumId, 1, 14) from ProblemType pt where pt.curriculumId in (:subSectionList)")
	List<String> findAllSection(@Param("subSectionList") List<String> subSectionList);

	@Query("select substr(pt.curriculumId, 1, 14) from ProblemType pt where pt.curriculumId in (:subSectionList) and pt.typeId not in (:completedTypeIdList) order by pt.typeId asc")
	List<String> findAllSectionNotInTypeList(@Param("subSectionList") List<String> subSectionList,
		@Param("completedTypeIdList") List<Integer> completedTypeIdList);

	@Query("select pt.typeId from ProblemType pt where pt.curriculumId in (:subSectionList) order by pt.typeId asc")
	List<Integer> findAllExamTypeIdList(@Param("subSectionList") List<String> subSectionList);

	@Query("select pt.typeId from ProblemType pt where pt.curriculumId in (:subSectionList) and pt.typeId not in (:completedTypeIdList) order by pt.typeId asc")
	List<Integer> findRemainTypeIdList(@Param("subSectionList") List<String> subSectionList,
		@Param("completedTypeIdList") List<Integer> completedTypeIdList);

	@Query("select pt.typeName from ProblemType pt where pt.typeId=:typeId")
	String findTypeNameById(@Param("typeId") Integer typeId);

	@Query("select pt.curriculum.subSection from ProblemType pt where pt.typeId=:typeId")
	String findSubSectionNameById(@Param("typeId") Integer typeId);

	//신
	@Query("select pt from ProblemType pt where (coalesce(:subSectionList, null) is null or pt.curriculumId in (:subSectionList)) order by pt.curriculum.curriculumSequence asc, pt.sequence asc")
	List<ProblemType> findAllTypeInSubSectionList(@Param("subSectionList") List<String> subSectionList);

	@Query("select pt.typeId from ProblemType pt where pt.curriculumId = :subSection order by pt.curriculum.curriculumSequence asc, pt.sequence asc")
	List<Integer> findAllTypeIdInSubSection(@Param("subSection") String subSection);
	
	@Query("select pt.typeId from ProblemType pt where pt.curriculumId like concat(:section, '%') order by pt.curriculum.curriculumSequence asc, pt.sequence asc")
	List<Integer> findAllTypeIdInSection(@Param("section") String section);

}

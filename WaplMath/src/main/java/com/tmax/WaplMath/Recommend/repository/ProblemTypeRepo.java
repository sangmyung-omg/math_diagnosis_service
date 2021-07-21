package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import java.util.Set;
import com.tmax.WaplMath.Common.model.problem.ProblemType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("RE-ProblemTypeRepo")
public interface ProblemTypeRepo extends CrudRepository<ProblemType, Integer> {


	@Query("select substr(pt.curriculumId, 1, 14) from ProblemType pt where pt.curriculumId in (:subSectionList)")
	List<String> findAllSection(@Param("subSectionList") List<String> subSectionList);

	@Query("select substr(pt.curriculumId, 1, 14) from ProblemType pt where pt.curriculumId in (:subSectionList) and pt.typeId not in (:completedTypeIdList) order by pt.typeId asc")
	List<String> findAllSectionNotInTypeList(@Param("subSectionList") List<String> subSectionList,
		@Param("completedTypeIdList") List<Integer> completedTypeIdList);

	@Query("select pt.typeId from ProblemType pt where pt.curriculumId in (:subSectionList) and pt.typeId not in (:completedTypeIdList) order by pt.typeId asc")
	List<Integer> findRemainTypeIdList(@Param("subSectionList") List<String> subSectionList,
		@Param("completedTypeIdList") List<Integer> completedTypeIdList);

	@Query("select pt.typeName from ProblemType pt where pt.typeId=:typeId")
	String findTypeNameById(@Param("typeId") Integer typeId);

	@Query("select pt.curriculum.subSection from ProblemType pt where pt.typeId=:typeId")
	String findSubSectionNameById(@Param("typeId") Integer typeId);

	//v1
	@Query("select pt from ProblemType pt where (coalesce(:subSectionList, null) is null or pt.curriculumId in (:subSectionList)) order by pt.curriculum.curriculumSequence asc, pt.sequence asc")
	List<ProblemType> findTypeListInSubSectionList(@Param("subSectionList") List<String> subSectionList);

	@Query("select pt.typeId from ProblemType pt where pt.curriculumId in (:subSectionList) order by pt.typeId asc")
	List<Integer> findTypeIdListInSubSectionList(@Param("subSectionList") List<String> subSectionList);

	@Query("select pt.typeId from ProblemType pt where pt.curriculumId = :subSection order by pt.curriculum.curriculumSequence asc, pt.sequence asc")
	List<Integer> findTypeIdListInSubSection(@Param("subSection") String subSection);

	@Query("select count(pt) from ProblemType pt where pt.curriculumId = :subSection")
	Integer findTypeCntInSubSection(@Param("subSection") String subSection);

	@Query("select pt.typeId from ProblemType pt where pt.curriculumId like concat(:section, '%') order by pt.curriculum.curriculumSequence asc, pt.sequence asc")
	List<Integer> findTypeIdListInSection(@Param("section") String section);

	@Query("select pt from ProblemType pt where (coalesce(:subSectionList, null) is null or pt.curriculumId in (:subSectionList)) and (coalesce(:completedTypeIdList, null) is null or pt.typeId not in (:completedTypeIdList)) order by pt.curriculum.curriculumSequence asc, pt.sequence asc")
	List<ProblemType> NfindRemainTypeIdList(@Param("subSectionList") List<String> subSectionList, @Param("completedTypeIdList") List<Integer> completedTypeIdList);

	@Query("select pt.typeName from ProblemType pt where pt.typeId=:typeId")
	String NfindTypeNameById(@Param("typeId") Integer typeId);

	@Query("select distinct pt.curriculumId from ProblemType pt where (coalesce(:typeIdList, null) is null or pt.typeId in (:typeIdList))")
	List<String> findSubSectionListInTypeList(@Param("typeIdList") List<Integer> typeIdList);

	//21.07.01 card generator v2
	@Query("select pt.curriculumId as currId from ProblemType pt where pt.frequent='true' and pt.curriculumId like concat(:sectionId, '%') group by pt.curriculumId order by count(pt) desc")
	List<String> findSubSectionIdListInSectionOrderByFreq(@Param("sectionId") String sectionId);

	@Query("select substr(pt.curriculumId, 1, 14) as currId from ProblemType pt where pt.frequent='true' and pt.curriculumId like concat(:chapterId, '%') group by substr(pt.curriculumId, 1, 14) order by count(pt) desc")
	List<String> findSectionIdListInChapterOrderByFreq(@Param("chapterId") String chapterId);

	@Query("select substr(pt.curriculumId, 1, 11) as currId from ProblemType pt where pt.frequent='true' and (coalesce(:subSectionIdSet, null) is null or pt.curriculumId in (:subSectionIdSet)) group by substr(pt.curriculumId, 1, 11) order by count(pt) desc")
	List<String> findChapterListInSubSectionSetOrderByFreq(@Param("subSectionIdSet") Set<String> subSectionIdSet);

	@Query("select pt.typeId from ProblemType pt where pt.curriculumId = :subSection and pt.frequent='true' order by pt.curriculum.curriculumSequence asc, pt.sequence asc")
	List<Integer> findFreqTypeIdListInSubSection(@Param("subSection") String subSection);

	@Query("select pt.typeId from ProblemType pt where (coalesce(:typeIdSet, null) is null or pt.typeId in (:typeIdSet)) order by pt.sequence asc")
	List<Integer> sortByTypeSeq(@Param("typeIdSet") Set<Integer> typeIdSet);
}

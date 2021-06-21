package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.problem.Problem;

public interface ProblemRepo extends CrudRepository<Problem, Integer> {
	@Query("select distinct p.typeId from Problem p where p.probId in (:probIdList)")
	public List<Integer> findAllTypeId(@Param("probIdList") List<Integer> probIdList);

	@Query("select distinct substr(p.problemType.curriculumId, 0, 14) from Problem p where p.probId in (:probIdList)")
	public List<String> findAllSectionId(@Param("probIdList") List<Integer> probIdList);

	@Query("select p from Problem p where substr(p.problemType.curriculumId, 0, 14)=:sectionId")
	public List<Problem> findAllProbBySection(@Param("sectionId") String sectionId);

	@Query("select p from Problem p where substr(p.problemType.curriculumId, 0, 14)=:sectionId and p.probId not in (:probIdList)")
	public List<Problem> findAllProbBySectionNotInList(@Param("sectionId") String sectionId,
			@Param("probIdList") List<Integer> probIdList);

	@Query("select p from Problem p where p.typeId=:typeId")
	public List<Problem> findAllProbByType(@Param("typeId") Integer typeId);

	@Query("select p from Problem p where p.typeId=:typeId and p.probId not in (:probIdList)")
	public List<Problem> findAllProbByTypeNotInList(@Param("typeId") Integer typeId,
			@Param("probIdList") List<Integer> probIdList);

	@Query("select p from Problem p where p.problemType.curriculumId in (:subSectionList)")
	public List<Problem> findAllProbBySubSectionList(@Param("subSectionList") List<String> subSectionList);

	@Query("select p from Problem p where p.problemType.curriculumId in (:subSectionList) and p.probId not in (:probIdList)")
	public List<Problem> findAllProbBySubSectionListNotInList(@Param("subSectionList") List<String> subSectionList,
			@Param("probIdList") List<Integer> probIdList);

	@Query("select p.probId from Problem p where p.typeId=:typeId")
	public List<Integer> findAllProbIdByType(@Param("typeId") Integer typeId);

	@Query("select p.probId from Problem p where p.typeId=:typeId and p.probId not in (:probIdList)")
	public List<Integer> findAllProbIdByTypeNotInList(@Param("typeId") Integer typeId,
			@Param("probIdList") List<Integer> probIdList);

	
	//신
	@Query("select p from Problem p where p.typeId=:typeId and (coalesce(:solvedProbIdSet, null) is null or p.probId not in (:solvedProbIdSet))")
	public List<Problem> NfindProbListByType(@Param("typeId") Integer typeId, @Param("solvedProbIdSet") Set<Integer> solvedProbIdSet);
	
	@Query("select p from Problem p where p.problemType.curriculumId in (:subSectionList) and (coalesce(:solvedProbIdSet, null) is null or p.probId not in (:solvedProbIdSet))")
	public List<Problem> findProbListInSubSectionList(@Param("subSectionList") List<String> subSectionList, @Param("solvedProbIdSet") Set<Integer> solvedProbIdSet);
	
	@Query("select p from Problem p where p.typeId=:typeId and (coalesce(:solvedProbIdList, null) is null or p.probId not in (:solvedProbIdList))")
	public List<Problem> NfindAllProbByType(@Param("typeId") Integer typeId, @Param("solvedProbIdList") List<Integer> solvedProbIdList);


	//2021-06-17 Added by Jonghyun Seong. gets Problem List from probIDList
	@Query("select p from Problem p where p.probId in :probIdList")
	public List<Problem> getProblemsByProbIdList(@Param("probIdList") List<Integer> probIdList);
}

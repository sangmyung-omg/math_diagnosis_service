package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.problem.ProblemType;

public interface TypeUkRepository extends CrudRepository<ProblemType, Integer> {
	@Query(value = "select type_uk_uuid, type_uk_name, type_uk_description, substr(curriculum_id, 1, 14) curriculum_id from type_uk_master where substr(curriculum_id, 1, 11) in :chapterList order by type_uk_uuid asc", nativeQuery = true)
	List<ProblemType> findAllByCurriculum(@Param("chapterList") List<String> chapterList);

	@Query(value = "select substr(curriculum_id, 1, 14) from type_uk_master where substr(curriculum_id, 1, 11) in :chapterList order by type_uk_uuid asc", nativeQuery = true)
	List<String> findAllSection(@Param("chapterList") List<String> chapterList);

	@Query(value = "select substr(curriculum_id, 1, 14) from type_uk_master where substr(curriculum_id, 1, 11) in :chapterList and type_uk_uuid not in :userCompletedTypeUkList order by type_uk_uuid asc", nativeQuery = true)
	List<String> findAllSectionNotInList(@Param("chapterList") List<String> chapterList,
			@Param("userCompletedTypeUkList") List<String> userCompletedTypeUkList);

	@Query(value = "select type_uk_uuid from type_uk_master where substr(curriculum_id, 1, 11) in :chapterList and type_uk_uuid not in :userCompletedTypeUkIdList order by type_uk_uuid asc", nativeQuery = true)
	List<String> findNotCompletedTypeUkIdList(@Param("chapterList") List<String> chapterList,
			@Param("userCompletedTypeUkIdList") List<String> userCompletedTypeUkIdList);

	@Query(value = "select type_uk_uuid from type_uk_master where substr(curriculum_id, 1, 11) in :chapterList order by type_uk_uuid asc", nativeQuery = true)
	List<String> findTypeUkIdList(@Param("chapterList") List<String> chapterList);
	
	@Query(value = "select type_uk_name from type_uk_master where type_uk_uuid=:typeUkUuid", nativeQuery = true)
	String findTypeUkNameById(@Param("typeUkUuid") String typeUkUuid);
}

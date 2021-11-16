package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import java.util.Set;
import com.tmax.WaplMath.Common.model.uk.Uk;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("RE-UkRepo")
public interface UkRepo extends CrudRepository<Uk, Integer>{
	
	@Query("SELECT ud FROM Uk ud WHERE SUBSTR(ud.curriculumId, 1, 11) = ?1 order by ud.curriculumId")
	List<Uk> findAllByCurriculumId(String chapterId);

	//2021-06-25 Jonghyun Seong. Override findAll method to List type return.
	@Override
	List<Uk> findAll();

	//2021-07-02 Jonghyun_seong find by exact curriculum ID
	@Query("select u from Uk u where u.curriculumId = :currID")
	List<Uk> findByCurriculumId(@Param("currID") String currID);

	//2021-07-06 Jonghyun_seong find by likely curriculum ID
	@Query("select u from Uk u where u.curriculumId like concat(:currID, '%')")
	List<Uk> findByLikelyCurriculumId(@Param("currID") String currID);

  @Query("select distinct u.ukId from Uk u where u.curriculum.schoolType = '중등'")
  Set<Integer> findMiddleUkIdList();
}

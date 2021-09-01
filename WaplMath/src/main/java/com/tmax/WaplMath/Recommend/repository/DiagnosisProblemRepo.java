package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import com.tmax.WaplMath.Common.model.problem.DiagnosisProblem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("RE-DiagnosisProblemRepo")
public interface DiagnosisProblemRepo extends CrudRepository<DiagnosisProblem, Integer>{
	
  // 2021-09-01 Modified by Sangheon Lee. Get probs modified before today
	@Query("SELECT dp FROM DiagnosisProblem dp WHERE (SUBSTR(dp.basicProblem.problemType.curriculumId, 0, 11) = ?1)"
			+ " AND dp.basicProblem.status = 'ACCEPT'"
			+ " AND dp.upperProblem.status = 'ACCEPT'"
//			+ " AND (dp.lowerProblem.status IS NULL OR dp.lowerProblem.status = 'ACCEPT')"
			+ " AND dp.basicProblem.category = ?2"
			+ " AND dp.upperProblem.category = ?2"
      + " and (dp.basicProblem.editDate is null or dp.basicProblem.editDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.basicProblem.validateDate is null or dp.basicProblem.validateDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.upperProblem.editDate is null or dp.upperProblem.editDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.upperProblem.validateDate is null or dp.upperProblem.validateDate < to_date(?3, 'yyyy-MM-dd'))"
//			+ " AND (dp.lowerProblem.category IS NULL OR dp.lowerProblem.category = ?2)"
			+ " ORDER BY dp.basicProblem.problemType.curriculumId")
	List<DiagnosisProblem> findAllByChapter(String chapter, String diagType, String today);

  
  // 2021-09-01 Modified by Sangheon Lee. Get probs modified before today
	@Query("SELECT dp FROM DiagnosisProblem dp WHERE (SUBSTR(dp.basicProblem.problemType.curriculumId, 0, 11) IN ?1)"
			+ " AND dp.basicProblem.status = 'ACCEPT'"
			+ " AND dp.upperProblem.status = 'ACCEPT'"
//			+ " AND (dp.lowerProblem.status IS NULL OR dp.lowerProblem.status = 'ACCEPT')"
			+ " AND dp.basicProblem.category = ?2"
			+ " AND dp.upperProblem.category = ?2"
      + " and (dp.basicProblem.editDate is null or dp.basicProblem.editDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.basicProblem.validateDate is null or dp.basicProblem.validateDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.upperProblem.editDate is null or dp.upperProblem.editDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.upperProblem.validateDate is null or dp.upperProblem.validateDate < to_date(?3, 'yyyy-MM-dd'))"
//			+ " AND (dp.lowerProblem.category IS NULL OR dp.lowerProblem.category = ?2)"
			+ " ORDER BY dp.basicProblem.problemType.curriculumId")
	List<DiagnosisProblem> findAllByChapterIn(List<String> chapters, String diagType, String today);

  
  // 2021-09-01 Modified by Sangheon Lee. Get probs modified before today
	@Query("SELECT dp FROM DiagnosisProblem dp WHERE ((SUBSTR(dp.basicProblem.problemType.curriculumId, 0, 11) IN ?1)"
			+ " OR (dp.basicProblem.problemType.curriculumId IN ?1))"
			+ " AND dp.basicProblem.status = 'ACCEPT'"
			+ " AND dp.upperProblem.status = 'ACCEPT'"
//			+ " AND (dp.lowerProblem.status IS NULL OR dp.lowerProblem.status = 'ACCEPT')"
			+ " AND dp.basicProblem.category = ?2"
			+ " AND dp.upperProblem.category = ?2"
      + " and (dp.basicProblem.editDate is null or dp.basicProblem.editDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.basicProblem.validateDate is null or dp.basicProblem.validateDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.upperProblem.editDate is null or dp.upperProblem.editDate < to_date(?3, 'yyyy-MM-dd'))"
      + " and (dp.upperProblem.validateDate is null or dp.upperProblem.validateDate < to_date(?3, 'yyyy-MM-dd'))"
//			+ " AND (dp.lowerProblem.category IS NULL OR dp.lowerProblem.category = ?2)"
			+ " ORDER BY dp.basicProblem.problemType.curriculumId")
	List<DiagnosisProblem> findAllByChapterInIncludingElementary(List<String> chapters, String diagType, String today);
	

	List<DiagnosisProblem> findAllByBasicProblemProblemTypeCurriculumChapterInAndBasicProblemCategory(List<String> chapters, String diagType);
}

package com.tmax.WaplMath.AnalysisReport.repository.legacy.problem;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.model.problem.ProblemCurriculum;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemCurriculumRepo extends CrudRepository<ProblemCurriculum,String>{
    @Query(value = "select cm.CURRICULUM_ID,PROB_ID,SEQUENCE,TYPE_NAME,SCHOOL_TYPE,GRADE,SEMESTER,CHAPTER,SECTION,SUB_SECTION,PART from curriculum_master cm " +
            "join PROBLEM_TYPE_MASTER pm on cm.curriculum_id = pm.curriculum_id " +
            "join problem p on p.TYPE_ID = pm.TYPE_ID", nativeQuery=true)
    List<ProblemCurriculum> getAllCurriculumProblemMapping(@Param("userId") String userId);

    @Query(value = "select cm.CURRICULUM_ID,PROB_ID,SEQUENCE,TYPE_NAME,SCHOOL_TYPE,GRADE,SEMESTER,CHAPTER,SECTION,SUB_SECTION,PART from curriculum_master cm " +
            "join PROBLEM_TYPE_MASTER pm on cm.curriculum_id = pm.curriculum_id " +
            "join problem p on p.TYPE_ID = pm.TYPE_ID " + 
            "where prob_id in (:probIDList)", nativeQuery=true)
    List<ProblemCurriculum> getCurriculumProblemMappingOfProbIDList(@Param("probIDList") List<String> probIDList);
}

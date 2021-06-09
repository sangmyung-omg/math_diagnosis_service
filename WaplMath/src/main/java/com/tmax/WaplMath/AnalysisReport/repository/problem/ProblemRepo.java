package com.tmax.WaplMath.AnalysisReport.repository.problem;

import java.util.List;

import com.tmax.WaplMath.Recommend.model.problem.Problem;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("AR-ProblemRepo")
public interface ProblemRepo extends CrudRepository<Problem, Integer> {
    @Query("select prob from Problem prob INNER JOIN ProblemType pt on prob.problemType = pt.typeId where pt.curriculumId like :currID")
    public List<Problem> getProblemByCurriculumID(@Param("currID") String currID);
 
    @Query("select p from Problem p where p.probId in (:probIdList)")
    public List<Problem> getProblemByProbIDList(@Param("probIdList") List<String> probIdList);
}

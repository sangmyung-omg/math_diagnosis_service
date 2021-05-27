package com.tmax.WaplMath.AnalysisReport.repository.curriculum;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserCurriculumRepo extends CrudRepository<UserMasteryCurriculum,String> {
    @Query(value="", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculum(@Param("userID") String userID);
}

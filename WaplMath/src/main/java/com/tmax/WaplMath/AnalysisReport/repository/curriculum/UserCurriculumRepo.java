package com.tmax.WaplMath.AnalysisReport.repository.curriculum;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;
import com.tmax.WaplMath.Recommend.model.UserKnowledgeKey;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserCurriculumRepo extends CrudRepository<UserMasteryCurriculum,UserKnowledgeKey> {
    @Query(value="select know.USER_UUID, UK_MASTERY, know.UK_ID as UK_ID, UK_NAME, cm.CURRICULUM_ID as CURRICULUM_ID, CHAPTER, SECTION, CURRICULUM_SEQUENCE from " +
                 "user_knowledge know " +
                 "join uk_master ukm on know.uk_id=ukm.uk_id "+
                 "join curriculum_master cm on cm.curriculum_id = ukm.curriculum_id " + 
                 "where user_uuid = :userID " + 
                 "order by curriculum_sequence", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculum(@Param("userID") String userID);

    @Query(value="select know.USER_UUID, UK_MASTERY, know.UK_ID as UK_ID, UK_NAME, cm.CURRICULUM_ID as CURRICULUM_ID, CHAPTER, SECTION, CURRICULUM_SEQUENCE from " +
                 "user_knowledge know " +
                 "join uk_master ukm on know.uk_id=ukm.uk_id "+
                 "join curriculum_master cm on cm.curriculum_id = ukm.curriculum_id " + 
                 "where user_uuid = :userID " + 
                 "and cm.curriculum_id like :currRange " +
                 "order by uk_id", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculumWithCurrRange(@Param("userID") String userID, @Param("currRange") String currRange);
}

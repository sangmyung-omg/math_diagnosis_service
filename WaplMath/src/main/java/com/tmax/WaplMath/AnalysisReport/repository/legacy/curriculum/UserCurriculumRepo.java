package com.tmax.WaplMath.AnalysisReport.repository.legacy.curriculum;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledgeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserCurriculumRepo extends CrudRepository<UserMasteryCurriculum,UserKnowledgeKey> {
    @Query(value="select know.USER_UUID, UK_MASTERY, know.UK_ID as UK_ID, UK_NAME, cm.CURRICULUM_ID as CURRICULUM_ID, CHAPTER, SECTION, SUB_SECTION, CURRICULUM_SEQUENCE from " +
                 "user_knowledge know " +
                 "join uk_master ukm on know.uk_id=ukm.uk_id "+
                 "join curriculum_master cm on cm.curriculum_id = ukm.curriculum_id " + 
                 "where user_uuid = :userID " + 
                 "order by curriculum_sequence", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculum(@Param("userID") String userID);

    @Query(value="select know.USER_UUID, UK_MASTERY, know.UK_ID as UK_ID, UK_NAME, cm.CURRICULUM_ID as CURRICULUM_ID, CHAPTER, SECTION, SUB_SECTION,  CURRICULUM_SEQUENCE from " +
                 "user_knowledge know " +
                 "join uk_master ukm on know.uk_id=ukm.uk_id "+
                 "join curriculum_master cm on cm.curriculum_id = ukm.curriculum_id " + 
                 "where user_uuid = :userID " + 
                 "and cm.curriculum_id like :currRange " +
                 "order by curriculum_sequence", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculumWithCurrRange(@Param("userID") String userID, @Param("currRange") String currRange);


    @Query(value="select know.USER_UUID, UK_MASTERY, know.UK_ID as UK_ID, UK_NAME, cm.CURRICULUM_ID as CURRICULUM_ID, CHAPTER, SECTION, SUB_SECTION,  CURRICULUM_SEQUENCE from " +
                 "user_knowledge know " +
                 "join uk_master ukm on know.uk_id=ukm.uk_id "+
                 "join curriculum_master cm on cm.curriculum_id = ukm.curriculum_id " + 
                 "where user_uuid = :userID " + 
                 "and cm.curriculum_id in :currIDList " +
                 "order by curriculum_sequence", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculumWithCurrIDList(@Param("userID") String userID, @Param("currIDList") List<String> currIDList);

    @Query(value="select know.USER_UUID, UK_MASTERY, know.UK_ID as UK_ID, UK_NAME, cm.CURRICULUM_ID as CURRICULUM_ID, CHAPTER, SECTION, SUB_SECTION,  CURRICULUM_SEQUENCE from " +
                 "uk_master ukm " +
                 "join user_knowledge know  on know.uk_id=ukm.uk_id "+
                 "join curriculum_master cm on cm.curriculum_id = ukm.curriculum_id " + 
                 "where user_uuid = :userID " + 
                 "and know.UK_ID in :ukIDList " +
                 "order by curriculum_sequence", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculumWithUkIDList(@Param("userID") String userID, @Param("ukIDList") List<String> ukIDList);

    @Query(value="select know.USER_UUID, UK_MASTERY, know.UK_ID as UK_ID, UK_NAME, cm.CURRICULUM_ID as CURRICULUM_ID, CHAPTER, SECTION, SUB_SECTION,  CURRICULUM_SEQUENCE from " +
                 "uk_master ukm " +
                 "join curriculum_master cm on cm.curriculum_id = ukm.curriculum_id " + 
                 "join user_knowledge know  on know.uk_id=ukm.uk_id "+                 
                 "where user_uuid = :userID " + 
                 "and know.UK_ID = :ukID " +
                 "order by curriculum_sequence", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculumWithUkID(@Param("userID") String userID, @Param("ukID") String ukID);

    @Query(value="select know.USER_UUID, UK_MASTERY, know.UK_ID as UK_ID, UK_NAME, cm.CURRICULUM_ID as CURRICULUM_ID, CHAPTER, SECTION, SUB_SECTION,  CURRICULUM_SEQUENCE from " +
                 "uk_rel urel " +
                 "join uk_master ukm on urel.pre_uk_id=ukm.uk_id " +
                 "join curriculum_master cm on cm.curriculum_id = ukm.curriculum_id " + 
                 "join user_knowledge know  on know.uk_id=ukm.uk_id "+        
                 "where user_uuid = :userID " + 
                 "and urel.BASE_UK_ID = :ukID and urel.relation_reference = '선수' " +
                 "order by curriculum_sequence", nativeQuery=true)
    List<UserMasteryCurriculum> getUserCurriculumOfPreUKWithUkID(@Param("userID") String userID, @Param("ukID") String ukID);
}

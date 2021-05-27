package com.tmax.WaplMath.AnalysisReport.repository.uk;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.model.knowledge.UserKnowledgeJoined;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserKnowledgeRepo extends CrudRepository<UserKnowledgeJoined, String>{
    
    @Query(value="select USER_UUID, know.UK_ID, UK_MASTERY, UK_NAME, UK_DESCRIPTION, CURRICULUM_ID, PROB_ID from user_knowledge know " +
                 "join uk_master ukm on know.uk_id = ukm.uk_id " +
                 "join problem_uk_rel pur on pur.uk_id = ukm.uk_id " +
                 "where user_uuid=:userID " +
                 "and prob_id in (:probIDList)", nativeQuery = true)
    List<UserKnowledgeJoined> getUKListOfProbList(@Param("userID") String userID,@Param("probIDList") List<String> probIDList);

    @Query(value="select USER_UUID, know.UK_ID, UK_MASTERY, UK_NAME, UK_DESCRIPTION, CURRICULUM_ID, PROB_ID from user_knowledge know " +
                 "join uk_master ukm on know.uk_id = ukm.uk_id " +
                 "join problem_uk_rel pur on pur.uk_id = ukm.uk_id " +
                 "where user_uuid=:userID", nativeQuery = true)
    List<UserKnowledgeJoined> getUKList(@Param("userID") String userID);
}

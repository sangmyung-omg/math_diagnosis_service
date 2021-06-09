package com.tmax.WaplMath.AnalysisReport.repository.knowledge;

import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository("AR-UserKnowledgeRepo")
public interface UserKnowledgeRepo extends CrudRepository<UserKnowledge, String> {
    @Query("select know from UserKnowledge know where know.userUuid = :userID")
    public List<UserKnowledge> getUserKnowledge(@Param("userID") String userID);


    @Query("select know from UserKnowledge know join Uk uk on uk.ukId = know.ukId where know.userUuid = :userID and uk.curriculumId in :currIDList")
    public List<UserKnowledge> getKnowledgeOfCurrList(@Param("userID") String userID, @Param("currIDList") List<String> currIDList);

    @Query("select know from UserKnowledge know join Uk uk on uk.ukId = know.ukId where know.userUuid = :userID and uk.curriculumId like concat(:currIdLike,'%')")
    public List<UserKnowledge> getKnowledgeOfCurrLike(@Param("userID") String userID, @Param("currIdLike") String currIdLike);

    @Query("select know from UserKnowledge know join Uk uk on uk.ukId = know.ukId where know.userUuid = :userID and uk.curriculumId = :currId order by uk.ukId")
    public List<UserKnowledge> getKnowledgeOfCurrID(@Param("userID") String userID, @Param("currId") String currId);
}
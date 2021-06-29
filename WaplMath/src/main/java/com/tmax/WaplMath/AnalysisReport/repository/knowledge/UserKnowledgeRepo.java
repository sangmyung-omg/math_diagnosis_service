package com.tmax.WaplMath.AnalysisReport.repository.knowledge;

import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledgeKey;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for user knowledge mastery related to user + extra conditions
 * @author Jonghyun Seong
 */
@Repository("AR-UserKnowledgeRepo")
public interface UserKnowledgeRepo extends CrudRepository<UserKnowledge, UserKnowledgeKey> {
    @Query("select know from UserKnowledge know where know.userUuid = :userID")
    public List<UserKnowledge> getUserKnowledge(@Param("userID") String userID);


    @Query("select know from UserKnowledge know join Uk uk on uk.ukId = know.ukId where know.userUuid = :userID and uk.curriculumId in :currIDList")
    public List<UserKnowledge> getKnowledgeOfCurrList(@Param("userID") String userID, @Param("currIDList") List<String> currIDList);

    @Query("select know from UserKnowledge know join Uk uk on uk.ukId = know.ukId where know.userUuid = :userID and uk.curriculumId like concat(:currIdLike,'%')")
    public List<UserKnowledge> getKnowledgeOfCurrLike(@Param("userID") String userID, @Param("currIdLike") String currIdLike);

    @Query("select know from UserKnowledge know join Uk uk on uk.ukId = know.ukId where know.userUuid = :userID and uk.curriculumId = :currId order by uk.ukId")
    public List<UserKnowledge> getKnowledgeOfCurrID(@Param("userID") String userID, @Param("currId") String currId);

    //2021-06-29 jonghyun_seong. To get uk by curriculum
    @Query("select know from UserKnowledge know join Uk uk on uk.ukId = know.ukId where uk.curriculumId = :currId order by know.userUuid")
    public List<UserKnowledge> getAllByCurrID(@Param("currId") String currId);

    //2021-06-29 jonghyun_seong. To get lower bound curriculums too
    @Query("select know from UserKnowledge know join Uk uk on uk.ukId = know.ukId where uk.curriculumId like concat(:currId,'%') order by know.userUuid")
    public List<UserKnowledge> getAllByLikelyCurrID(@Param("currId") String currId);


    //2021-06-25 by Jonghyun Seong. Query to get specific UK_ID's user masterys
    @Query("Select know from UserKnowledge know where know.ukId = :ukId")
    public List<UserKnowledge> getByUkId(@Param("ukId") Integer ukId);

    //2021-06-28 New name for same service. 
    @Query("select know from UserKnowledge know where know.userUuid = :userID")
    public List<UserKnowledge> getByUserUuid(@Param("userID") String userID);
}
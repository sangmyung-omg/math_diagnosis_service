package com.tmax.WaplMath.Common.repository.knowledge;

import java.util.List;

// import javax.persistence.Entity;

import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledgeKey;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserKnowledgeRepo extends CrudRepository<UserKnowledge, UserKnowledgeKey> {
    /**
     * Added by Jonghyun seong. select by userID
     */
    @Query("select uknow from UserKnowledge uknow where uknow.userUuid=:userID")
    List<UserKnowledge> getByUserID(@Param("userID") String userID);

    @Query("select uknow from UserKnowledge uknow where uknow.userUuid in :userIDList")
    List<UserKnowledge> getByUserIDList(@Param("userIDList") List<String> userIDList);
}

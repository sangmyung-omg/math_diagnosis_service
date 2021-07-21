package com.tmax.WaplMath.Recommend.repository;

import com.tmax.WaplMath.Common.model.knowledge.UserEmbedding;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("RE-UserEmbeddingRepo")
public interface UserEmbeddingRepo extends CrudRepository<UserEmbedding, String> {
//	@Query("SELECT UE.userEmbedding FROM #{#entityName} UE WHERE UE.userUuid LIKE ?1")
//	CurriculumDAO findByUserUuid(String userUuid);

    /**
     * 2021-06-18 Added by Jonghyun Seong to get user's embedding data
     * @param userID user's UUID
     */
    @Query("Select ue from UserEmbedding ue where ue.userUuid = :userID")
    public UserEmbedding getEmbedding(@Param("userID") String userID);

    /**
     * 2021-06-18 Added by Jonghyun Seong to update user's embedding string
     * @param userID user's UUID
     * @param embedding the new embedding to update the field
     */
    @Modifying
    @Transactional
    @Query("Update UserEmbedding ue set ue.userEmbedding=:embedding, ue.updateDate=current_timestamp() where ue.userUuid=:userID")
    public int updateEmbedding(@Param("userID") String userID, @Param("embedding") String embedding);
}

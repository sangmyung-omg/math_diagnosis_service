package com.tmax.WaplMath.Recommend.repository;

import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.UserEmbedding;

public interface UserEmbeddingRepository extends CrudRepository<UserEmbedding, String> {
//	@Query("SELECT UE.userEmbedding FROM #{#entityName} UE WHERE UE.userUuid LIKE ?1")
//	CurriculumDAO findByUserUuid(String userUuid);

}

package com.tmax.Recommend.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserEmbeddingRepository extends CrudRepository<UserEmbeddingDAO, String> {
//	@Query("SELECT UE.userEmbedding FROM #{#entityName} UE WHERE UE.userUuid LIKE ?1")
//	CurriculumDAO findByUserUuid(String userUuid);

}

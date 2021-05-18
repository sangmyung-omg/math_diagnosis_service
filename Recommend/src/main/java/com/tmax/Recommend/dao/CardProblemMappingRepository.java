package com.tmax.Recommend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CardProblemMappingRepository extends CrudRepository<CardProblemMappingDAO, String> {
	
	@Query(value="select * from card_problem_mapping where card_id=:cardId order by prob_sequence asc",
			nativeQuery=true)
	List<CardProblemMappingDAO> findAllByCardId(@Param("cardId") String cardId);
	
}

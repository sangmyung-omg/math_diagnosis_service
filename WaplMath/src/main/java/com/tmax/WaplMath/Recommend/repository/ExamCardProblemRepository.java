package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.ExamCardProblem;
import com.tmax.WaplMath.Recommend.model.ExamCardProblemKey;

public interface ExamCardProblemRepository extends CrudRepository<ExamCardProblem, ExamCardProblemKey> {
	
	@Query(value="select * from card_problem_mapping where card_id=:cardId order by prob_sequence asc",
			nativeQuery=true)
	List<ExamCardProblem> findAllByCardId(@Param("cardId") String cardId);
	
}

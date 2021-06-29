package com.tmax.WaplMath.DBUpdate.repository.problem;


import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tmax.WaplMath.Recommend.model.problem.Problem;


public interface DBUpdateProblemRepository extends JpaRepository<Problem, Long> {
	
	Problem findByProbId(Long probId);
	
	List<Problem> findByValidateDateGreaterThanAndStatusIs(Timestamp time, String status);
	List<Problem> findByValidateDateGreaterThanAndStatusNot(Timestamp time, String status);
	
}

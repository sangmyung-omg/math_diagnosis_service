package com.tmax.WaplMath.Problem.repository;

import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Problem.model.Problem;



public interface ProblemRepository extends CrudRepository<Problem,Integer> {

	Problem findByProbId(Integer ProbId);
}

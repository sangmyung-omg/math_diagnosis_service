package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.ProblemSetDemo;

public interface ProblemSetRepository extends CrudRepository<ProblemSetDemo, String>{
	
	List<ProblemSetDemo> findAllByChapter(String chapter);
}

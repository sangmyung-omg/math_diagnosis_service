package com.tmax.WaplMath.Problem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tmax.WaplMath.Problem.repository.ProblemImageRepository;
import com.tmax.WaplMath.Problem.repository.ProblemRepository;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.model.problem.ProblemImage;

@Service
public class ProblemInfoService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	@Autowired
	private ProblemRepository problemRepository;
	
	@Autowired
	private ProblemImageRepository problemImageRepository;
	
	public Problem getProblemInfo(int probId) {
		Problem problem = new Problem();
		Optional<Problem> result = problemRepository.findById(probId);
		logger.info("Service probId = "+probId);
		
		if(!result.isPresent()) {
			logger.info("there is no results");
			logger.info("result = " + result.isPresent());
		}
		
		problem = result.get();
		
		System.out.println("problem = "+problem);
		return problem;
	}
	
	public ProblemImage getProblemImage(int probId) {
		ProblemImage problemimage = new ProblemImage();
		
		Optional<ProblemImage> result = problemImageRepository.findById(probId);
		logger.info("Service probId = "+probId);
		
		if(!result.isPresent()) {
			logger.info("there is no results");
			logger.info("result = " + result.isPresent());
		}
		
		problemimage = result.get();
		System.out.println("problem Image = "+problemimage);
		
		return problemimage;
	}
	
//	public Problem getProblemWithImage(int probId) {
//		Problem problem = new Problem();
//		Optional<Problem> result = problemRepository.findAllById(probId);
//		logger.info("Service probId = "+probId);
//		
//		if(!result.isPresent()) {
//			logger.info("there is no results");
//			logger.info("result = " + result.isPresent());
//		}
//		
//		problem = result.get();
//		
//		System.out.println("problem = "+problem);
//		return problem;
//	}
	
	

}

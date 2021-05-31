package com.tmax.WaplMath.Problem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.Problem.model.Problem;
import com.tmax.WaplMath.Problem.model.ProblemImage;
import com.tmax.WaplMath.Problem.service.ProblemInfoService;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class ProblemController {
	
	@Autowired
	ProblemInfoService problemInfoService = new ProblemInfoService();
	
	@GetMapping(value = "/ProblemInfo", produces = "application/json; charset=utf-8")
	public Problem getProblemInfo(
			@RequestParam String probId)
			throws Exception 
	{	

		log.info("probid = "+probId);
		
		int intId = Integer.parseInt(probId);
		
		return problemInfoService.getProblemInfo(intId);
	}
	
	
	@GetMapping(value = "/ProblemImage", produces = "application/json; charset=utf-8")
	public ProblemImage getProblemImage(
			@RequestParam String probId)
			throws Exception 
	{	
		
		int intId = Integer.parseInt(probId);
		
		return problemInfoService.getProblemImage(intId);
	}

}

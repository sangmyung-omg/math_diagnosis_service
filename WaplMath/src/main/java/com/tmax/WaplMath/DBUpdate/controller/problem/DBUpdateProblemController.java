package com.tmax.WaplMath.DBUpdate.controller.problem;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.DBUpdate.dto.problem.UpdatedProbInfoResponseDTO;
import com.tmax.WaplMath.DBUpdate.dto.problem.UpdatedProblemListResponseDTO;
import com.tmax.WaplMath.DBUpdate.service.problem.DBUpdateProblemComponent;

import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;

//@Log4j2
@RestController
@RequiredArgsConstructor
public class DBUpdateProblemController {
	
	private final DBUpdateProblemComponent dbUpdateApiComponent;
	
	/**
	 * 조회
	 * 
	 */
	
	@GetMapping("/UpdatedProblemList")
	public UpdatedProblemListResponseDTO updatedProblemList(
	    @RequestParam("date")
	    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
			
		try {
			List<List<String>> updatedLists = dbUpdateApiComponent.updatedProblemListComponent(dateTime);
	
		    return new UpdatedProblemListResponseDTO("200","success",updatedLists.get(0), updatedLists.get(1));
		}catch (Exception e) {
			e.printStackTrace();
			return new UpdatedProblemListResponseDTO("500","fail");
		}
	
	}
	
	@GetMapping("/UpdatedProblemInfo")
	public UpdatedProbInfoResponseDTO updatedProblemInfo(@RequestParam("probID") String probIdStr) {
			
		try {
			return dbUpdateApiComponent.problemsGetComponent(probIdStr);
		}catch (Exception e) {
			e.printStackTrace();
			return new UpdatedProbInfoResponseDTO("500","fail");
		}
	
	}

}

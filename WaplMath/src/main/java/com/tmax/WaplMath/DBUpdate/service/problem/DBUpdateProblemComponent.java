package com.tmax.WaplMath.DBUpdate.service.problem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.tmax.WaplMath.DBUpdate.dto.problem.UpdatedProbInfoResponseBaseDTO;
import com.tmax.WaplMath.DBUpdate.dto.problem.UpdatedProbInfoResponseDTO;
import com.tmax.WaplMath.DBUpdate.service.image.ImageFileServiceComponent;
import com.tmax.WaplMath.Recommend.model.problem.Problem;


import lombok.RequiredArgsConstructor;


@Controller
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DBUpdateProblemComponent {

	private final ImageFileServiceComponent imageFileServiceComponent;
	
	private final ProblemService problemService;

	
	/**
	 * 
	 * 특정 시간 이후 업데이트 된 문제 ID List 조회
	 */
	public List<List<String>> updatedProblemListComponent(
	    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
		
		try {
			List<List<String>> result = new ArrayList<List<String>>();
			
			List<Problem> acceptedList = problemService.findAcceptedProbIdsAfterInputTime(dateTime);
			List<Problem> elseList = problemService.findElseProbIdsAfterInputTime(dateTime);
			
			List<String> acceptedIdList = new ArrayList<String>();
			List<String> elseIdList = new ArrayList<String>();
	
			for(Problem acceptedProb:acceptedList) {
				acceptedIdList.add(acceptedProb.getProbId().toString());
			}
	
			for(Problem elseProb:elseList) {
				elseIdList.add(elseProb.getProbId().toString());
			}
			result.add(acceptedIdList);
			result.add(elseIdList);
		    return result;
		}catch (Exception e) {
			throw e;
		}
	
	}
	
	/**
	 * prob 문제 조회 
	 * probId List 받아 문제 조회, List<ProbGetResponse> 반환
	 */	
	public UpdatedProbInfoResponseDTO problemsGetComponent(String probIdStr){
		
		try {
			List<UpdatedProbInfoResponseBaseDTO> output = new ArrayList<UpdatedProbInfoResponseBaseDTO>();
			// set : probId []
			String[] strProbIdList = probIdStr.replace(" ","").split(",");
			
				for(String strProbId : strProbIdList) {
						Long probId = Long.parseLong(strProbId);
					
					//id로 Problem 정보 검색
					Problem findProblem = problemService.findOne(probId);
					
					//id로 이미지 정보 검색
					String imgJsonObjectToString = imageFileServiceComponent.getImgByProbIDServiceComponent(probId);
					
					//엔터티 -> DTO 변환
					UpdatedProbInfoResponseBaseDTO collect = new UpdatedProbInfoResponseBaseDTO(
							findProblem.getProbId().toString(), findProblem.getProblemType().getTypeId().toString(),
							findProblem.getProblemType().getCurriculum().getCurriculumId(),
							findProblem.getAnswerType(), findProblem.getLearningDomain(), findProblem.getQuestion(),
							findProblem.getSolution(), findProblem.getSource(), findProblem.getCorrectRate(), findProblem.getDifficulty(),
							findProblem.getStatus(), imgJsonObjectToString,
							findProblem.getTimeRecommendation(), findProblem.getFrequent() 
							);
					
					output.add(collect);
				}
				return new UpdatedProbInfoResponseDTO("200", "success", output);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	
	
	
		
}

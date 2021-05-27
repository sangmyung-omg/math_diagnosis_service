package com.tmax.WaplMath.AnalysisReport.controller.studyguide;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class StudyGuideControllerV0 {
    
    @GetMapping("/studyguide")
    ResponseEntity<Object> getStudyGuide(@RequestHeader("token") String token){
        List<StudyGuideDTO> outputList = new ArrayList<StudyGuideDTO>();
        
        StudyGuideDTO guide = new StudyGuideDTO();

        List<String> chapID = new ArrayList<String>();
        for(int i = 0 ; i < 5; i++){
            chapID.add(UUID.randomUUID().toString());
        }

        guide.setChapterIDList(chapID);
        guide.setCommentary("다음 시험을 위해서는 선수개념에 대한 개념을 보충할 필요가 있어요! 와플수학에서 %s학생을 위한 맞춤 커리큘럼을 준비해 놓았으니 다음 시험에는 90점까지 상승 가능할거에요");


        outputList.add(guide);
        
        return new ResponseEntity<>(outputList, HttpStatus.OK);
    }
}

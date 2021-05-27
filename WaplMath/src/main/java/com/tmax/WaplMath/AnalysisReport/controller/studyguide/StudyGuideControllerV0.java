package com.tmax.WaplMath.AnalysisReport.controller.studyguide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
import com.tmax.WaplMath.AnalysisReport.service.studyguide.StudyGuideServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.auth.JWTUtil;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class StudyGuideControllerV0 {

    @Autowired
    @Qualifier("StudyGuideServiceV0")
    StudyGuideServiceBase studySvc;
    
    @GetMapping("/studyguide")
    ResponseEntity<Object> getStudyGuide(@RequestHeader("token") String token){
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");

        List<StudyGuideDTO> outputList = new ArrayList<StudyGuideDTO>();
        outputList.add(studySvc.getStudyGuideOfUser(userID));
        
        return new ResponseEntity<>(outputList, HttpStatus.OK);
    }
}

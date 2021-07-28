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

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
import com.tmax.WaplMath.AnalysisReport.service.studyguide.StudyGuideServiceBase;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

/**
 * Study Guide REST Controller
 * @author Jonghyun Seong
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v0")
public class StudyGuideControllerV0 {

    @Autowired
    @Qualifier("AR-StudyGuideServiceV1")
    StudyGuideServiceBase studySvc;
    
    @GetMapping("/studyguide")
    ResponseEntity<Object> getStudyGuide(@RequestHeader("token") String token){
        String userID  = JWTUtil.getUserID(token, false);

        List<StudyGuideDTO> outputList = new ArrayList<StudyGuideDTO>();
        outputList.add(studySvc.getStudyGuideOfUser(userID));
        
        return new ResponseEntity<>(outputList, HttpStatus.OK);
    }
}

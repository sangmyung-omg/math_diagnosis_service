package com.tmax.WaplMath.AnalysisReport.controller.debug;

import java.util.Arrays;
import java.util.List;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.service.diagnosis.DiagnosisServiceV0;
import com.tmax.WaplMath.AnalysisReport.service.mastery.DebugMasteryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * Debug REST Controller for unit testing
 * @author Jonghyun Seong
 */
@RestController
@Slf4j
@RequestMapping(path=ARConstants.apiPrefix + "/v0")
public class DebugController {
    @Autowired private DebugMasteryService debugMasteryService;

    
    @PostMapping(value = "/debug/skilltest")
    public ResponseEntity<?> simulateTritonScore(@RequestBody String csvbody, @RequestParam("stride") Integer stride, 
                                                 @RequestParam("startCurriculumId") String startCurrId,
                                                 @RequestParam("endCurriculumId") String endCurrId,
                                                 @RequestParam(value = "excludeCurriculumIdList", defaultValue = " ") String excludeList){//@RequestBody HttpEntity<String> httpEntity){
        // String test = httpEntity.getBody();
        List<String> excludeCurrIdList = Arrays.asList(excludeList.split(","));
        Object result = debugMasteryService.simulateScoreFromLrsCSV(csvbody, stride, startCurrId, endCurrId, excludeCurrIdList);
        return ResponseEntity.ok().body(result);
    }


    @Autowired private DiagnosisServiceV0 diagnosisSvc;

    @GetMapping("/debug/lrsscore")
    public ResponseEntity<?> getScoreFromLRS(@RequestParam("userID") String userID){
        Object score = diagnosisSvc.getScore(userID);
        return ResponseEntity.ok(score);
    }
}

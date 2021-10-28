package com.tmax.WaplMath.AnalysisReport.controller.report;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataLiteDTO;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataV2DTO;
import com.tmax.WaplMath.AnalysisReport.service.report.ReportServiceBaseV0;
import com.tmax.WaplMath.AnalysisReport.service.report.ReportServiceV1;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.service.mastery.v2.MasteryServiceV2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path=ARConstants.apiPrefix + "/v3")
public class ReportControllerV3 {
    @Autowired
    MasteryServiceV2 masterySvc;

    @Autowired
    ReportServiceV1 reportSvc;

    @PutMapping("/report")
    public ResponseEntity<Object> getUpdatedReport(@RequestHeader("token") String token, @RequestParam(name="limit", defaultValue = "10") Integer limit, 
                                                   @RequestParam(name="topfirst", defaultValue = "false") boolean topfirst, @RequestParam(name="exclude", defaultValue = "") String exclude){
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
                
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        //Step 1: update mastery -> if error, the masterySvc will throw it itself
        masterySvc.updateMasteryWithLRS(userID);

        //Step 2:
        ReportDataV2DTO output = reportSvc.getReport(userID, limit, topfirst, excludeSet);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping("/report")
    public ResponseEntity<Object> getReport(@RequestHeader("token") String token, @RequestParam(name="limit", defaultValue = "10") Integer limit, 
                                            @RequestParam(name="topfirst", defaultValue = "false") boolean topfirst, @RequestParam(name="exclude", defaultValue = "") String exclude){
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
                
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        ReportDataV2DTO output = reportSvc.getReport(userID, limit, topfirst, excludeSet);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

}

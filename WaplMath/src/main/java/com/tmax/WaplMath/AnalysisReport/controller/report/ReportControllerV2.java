package com.tmax.WaplMath.AnalysisReport.controller.report;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataLiteDTO;
import com.tmax.WaplMath.AnalysisReport.service.report.ReportServiceBaseV0;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.LRSStatementEmptyException;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.service.mastery.v1.MasteryServiceBaseV1;

import com.tmax.WaplMath.Common.util.exception.StackPrinter;

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
@RequestMapping(path=ARConstants.apiPrefix + "/v2")
public class ReportControllerV2 {
    @Autowired
    MasteryServiceBaseV1 masterySvc;

    @Autowired
    ReportServiceBaseV0 reportSvc;

    @PutMapping("/updatedreport")
    public ResponseEntity<Object> getUpdatedReport(@RequestHeader("token") String token, @RequestParam(name="exclude", defaultValue = "") String exclude){
        //Parse jwt to get userID
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");
                
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        //Step 1: update mastery -> if error, the masterySvc will throw it itself
        masterySvc.updateMasteryFromLRS(token);

        //Step 2:
        ReportDataDTO output = reportSvc.getReport(userID, excludeSet);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping("/report")
    public ResponseEntity<Object> getReport(@RequestHeader("token") String token, @RequestParam(name="exclude", defaultValue = "") String exclude){
        //Parse jwt to get userID
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");
                
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        ReportDataDTO output = reportSvc.getReport(userID, excludeSet);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping("/reportlite")
    public ResponseEntity<Object> getReportLite(@RequestHeader("token") String token, @RequestParam(name="exclude", defaultValue = "") String exclude){
        //Parse jwt to get userID
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");
                
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        ReportDataLiteDTO output = reportSvc.getReportLite(userID, excludeSet);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }
}

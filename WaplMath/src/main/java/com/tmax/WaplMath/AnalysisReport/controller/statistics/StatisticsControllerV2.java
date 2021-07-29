package com.tmax.WaplMath.AnalysisReport.controller.statistics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.CorrectRateDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.SolveSpeedDTO;
import com.tmax.WaplMath.AnalysisReport.service.statistics.score.ScoreServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path=ARConstants.apiPrefix + "/v2")
public class StatisticsControllerV2 {
    @Autowired
    ScoreServiceBase scoreSvc;

    @GetMapping("/score")
    public ResponseEntity<Object> getScore(@RequestHeader("token") String token, 
                                               @RequestParam(defaultValue = "user", name="type") String type,
                                               @RequestParam(defaultValue = "", name="exclude") String exclude) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
        
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));
        
        PersonalScoreDTO result;
        switch(type){
            case "wapl":
                result = scoreSvc.getWaplScore(userID, excludeSet);
                break;
            case "target":
                result = scoreSvc.getTargetScore(userID, excludeSet);
                break;
            case "user":
                result = scoreSvc.getUserScore(userID, excludeSet);
                break;
            default:
                log.error("Invalid type: " + type);
                throw new GenericInternalException(ARErrorCode.INVALID_PARAMETER, "Invalid type: " + type);
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/scorestats")
    public ResponseEntity<Object> getScoreStats(@RequestHeader("token") String token, 
                                               @RequestParam(defaultValue = "", name="exclude") String exclude,
                                               @RequestParam(defaultValue = "11", name="histogramSize") Integer histogramSize,
                                               @RequestParam(defaultValue = "101", name="percentileLUTSize") Integer percentileLUTSize) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
        
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));
        
        GlobalStatisticDTO result = scoreSvc.getScoreStats(userID, excludeSet, Math.min(histogramSize, 1000), Math.min(percentileLUTSize, 1000));
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/solvespeed")
    public ResponseEntity<Object> getSolveSpeed(@RequestHeader("token") String token, 
                                               @RequestParam(defaultValue = "", name="exclude") String exclude) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
        
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));
        
        SolveSpeedDTO result = scoreSvc.getSolveSpeedRate(userID, excludeSet);
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/correctrate")
    public ResponseEntity<Object> getCorrectRate(@RequestHeader("token") String token, 
                                               @RequestParam(defaultValue = "", name="exclude") String exclude) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
        
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));
        
        CorrectRateDTO result = scoreSvc.getCorrectRate(userID, excludeSet);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

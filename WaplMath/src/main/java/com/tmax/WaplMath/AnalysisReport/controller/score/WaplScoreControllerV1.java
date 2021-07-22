package com.tmax.WaplMath.AnalysisReport.controller.score;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreServiceBaseV0;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v1")
public class WaplScoreControllerV1 {
    @Autowired
    @Qualifier("AR-WaplScoreServiceV0")
    WaplScoreServiceBaseV0 waplScoreSvcV0;


    @GetMapping("/waplscore")
    public ResponseEntity<Object> getWaplScore(@RequestHeader("token") String token) {
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");
        
        WAPLScoreDTO result = WAPLScoreDTO.getScaledScore( waplScoreSvcV0.getWaplScore(userID));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
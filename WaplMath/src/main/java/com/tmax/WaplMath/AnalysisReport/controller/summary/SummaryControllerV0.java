package com.tmax.WaplMath.AnalysisReport.controller.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.Generic500ErrorDTO;
import com.tmax.WaplMath.AnalysisReport.service.summary.SummaryServiceBase;

@RestController
@RequestMapping(path= Constants.apiPrefix + "/v0")
class SummaryControllerV0 {
    
    @Autowired
    @Qualifier("v0")
    private SummaryServiceBase summarySvc;

    @GetMapping("/summary")
    ResponseEntity<Object> getSummary(@RequestHeader(value="token", required = false) String token) {
        //TODO: get userID and previledge from token
        String userID = token; 

        if(token == null)  {
            return new ResponseEntity<>(new Generic500ErrorDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(summarySvc.getSummaryOfUser(userID),HttpStatus.OK);
    }
}
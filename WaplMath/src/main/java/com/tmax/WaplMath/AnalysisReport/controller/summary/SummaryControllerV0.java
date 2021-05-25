package com.tmax.WaplMath.AnalysisReport.controller.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;
import com.tmax.WaplMath.AnalysisReport.service.summary.SummaryServiceBase;

@RestController
@RequestMapping(path= Constants.apiPrefix + "/v0")
class SummaryControllerV0 {
    
    @Autowired
    @Qualifier("v0")
    private SummaryServiceBase summarySvc;

    @GetMapping("/summary")
    SummaryReportDTO getSummary(@RequestHeader("token") String token) {
        //TODO: get userID and previledge from token
        String userID = token;

        return summarySvc.getSummaryOfUser(userID);
    }
}
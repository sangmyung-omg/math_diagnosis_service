package com.tmax.WaplMath.AnalysisReport.controller.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;

@RestController
@RequestMapping(path= Constants.apiPrefix + "/v0")
class SummaryControllerV0 {
    @Autowired
    @GetMapping("/summary")
    SummaryReportDTO getSummary() {
        SummaryReportDTO output =  new SummaryReportDTO();
        output.setScore(100);
        output.setPercentile(100.0);
        return output;
    }
}
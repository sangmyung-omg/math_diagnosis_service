package com.tmax.WaplMath.AnalysisReport.controller.result;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.DiagnosisResultDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class ResultControllerV0 {
    
    @GetMapping("/result")
    DiagnosisResultDTO getResult() {
        DiagnosisResultDTO output = new DiagnosisResultDTO();

        return output;
    }
}

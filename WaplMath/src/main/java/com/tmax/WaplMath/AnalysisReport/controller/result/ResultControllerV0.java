package com.tmax.WaplMath.AnalysisReport.controller.result;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.server.PathParam;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/result/{userID}")
    DiagnosisResultDTO getUserIDResult(@PathParam("userID") String userID) {
        DiagnosisResultDTO output = new DiagnosisResultDTO();

        return output;
    }

    @PostMapping("/results")
    List<DiagnosisResultDTO> getUserIDResult(@RequestBody UserIDListDTO body) {
        List<DiagnosisResultDTO> outputList = new ArrayList<DiagnosisResultDTO>();

        return outputList;
    }
}

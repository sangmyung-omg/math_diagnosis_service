package com.tmax.WaplMath.AnalysisReport.controller.result;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.server.PathParam;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class ResultControllerV0 {
    
    @GetMapping("/result")
    ResponseEntity<Object> getResult(@RequestHeader("token") String token) {
        DiagnosisResultDTO output = new DiagnosisResultDTO();

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping("/result/{userID}")
    ResponseEntity<Object> getUserIDResult(@RequestHeader("token") String token, @PathParam("userID") String userID) {
        DiagnosisResultDTO output = new DiagnosisResultDTO();

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @PostMapping("/results")
    ResponseEntity<Object>  getUserIDResult(@RequestHeader("token") String token, @RequestBody UserIDListDTO body) {
        List<DiagnosisResultDTO> outputList = new ArrayList<DiagnosisResultDTO>();

        return new ResponseEntity<>(outputList, HttpStatus.OK);
    }
}

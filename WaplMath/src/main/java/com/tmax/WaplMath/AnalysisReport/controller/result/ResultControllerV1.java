package com.tmax.WaplMath.AnalysisReport.controller.result;

import java.util.List;

import javax.websocket.server.PathParam;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultV1DTO;
import com.tmax.WaplMath.AnalysisReport.service.result.ResultServiceBaseV1;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Result REST Controller
 * @author Jonghyun Seong
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v1")
public class ResultControllerV1 {

    @Autowired
    @Qualifier("ResultServiceV1")
    private ResultServiceBaseV1 resultSvc;
    
    @GetMapping("/result")
    ResponseEntity<Object> getResult(@RequestHeader("token") String token) {
        String userID = JWTUtil.getUserID(token);

        DiagnosisResultV1DTO output = resultSvc.getResultOfUserV1(userID);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping("/result/{userID}")
    ResponseEntity<Object> getUserIDResult(@RequestHeader("token") String token, @PathParam("userID") String userID) {
        DiagnosisResultV1DTO output = resultSvc.getResultOfUserV1(userID);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @PostMapping("/results")
    ResponseEntity<Object>  getUserIDResult(@RequestHeader("token") String token, @RequestBody UserIDListDTO userIDList) {
        List<DiagnosisResultDTO> outputList = resultSvc.getResultOfMultipleUsers(userIDList);

        return new ResponseEntity<>(outputList, HttpStatus.OK);
    }
}

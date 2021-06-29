package com.tmax.WaplMath.AnalysisReport.controller.result;

import java.util.List;

import javax.websocket.server.PathParam;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.service.result.ResultServiceBase;
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
@RequestMapping(path=ARConstants.apiPrefix + "/v0")
public class ResultControllerV0 {

    @Autowired
    @Qualifier("ResultServiceV0")
    private ResultServiceBase resultSvc;
    
    @GetMapping("/result")
    ResponseEntity<Object> getResult(@RequestHeader("token") String token) {
        String userID = JWTUtil.getJWTPayloadField(token, "userID");

        DiagnosisResultDTO output = resultSvc.getResultOfUser(userID);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping("/result/{userID}")
    ResponseEntity<Object> getUserIDResult(@RequestHeader("token") String token, @PathParam("userID") String userID) {
        DiagnosisResultDTO output = resultSvc.getResultOfUser(userID);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @PostMapping("/results")
    ResponseEntity<Object>  getUserIDResult(@RequestHeader("token") String token, @RequestBody UserIDListDTO userIDList) {
        List<DiagnosisResultDTO> outputList = resultSvc.getResultOfMultipleUsers(userIDList);

        return new ResponseEntity<>(outputList, HttpStatus.OK);
    }
}

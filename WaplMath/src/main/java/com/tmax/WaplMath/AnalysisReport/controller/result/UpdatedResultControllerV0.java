package com.tmax.WaplMath.AnalysisReport.controller.result;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.exception.GenericInternalException;
import com.tmax.WaplMath.AnalysisReport.service.result.ResultServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.service.mastery.MasteryServiceBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class UpdatedResultControllerV0 {

    @Autowired
    private MasteryServiceBase masterySvc;

    @Autowired
    @Qualifier("ResultServiceV0")
    private ResultServiceBase resultSvc;
    
    @PutMapping("/updatedresult")
    DiagnosisResultDTO putUpdatedResult(@RequestHeader("token") String token, @RequestBody ProblemSolveListDTO probSolveList){
        String userID = JWTUtil.getJWTPayloadField(token, "userID");


        //Step 1: update mastery
        try {
            masterySvc.updateMastery(userID, probSolveList.getProbIdList(), probSolveList.getCorrectList());
        }
        catch (Throwable e){
            throw new GenericInternalException("ERR-0002", "Mastery update Exception");
        }

        //Step 2:
        DiagnosisResultDTO output = null;
        try {
            output = resultSvc.getResultOfUser(userID);
        }
        catch (Throwable e){
            throw new GenericInternalException("ERR-0003", "Result get exception");
        }

        return output;
    }    
}
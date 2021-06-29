package com.tmax.WaplMath.AnalysisReport.controller.result;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.service.result.ResultServiceBase;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Common.util.exception.StackPrinter;
import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.service.mastery.MasteryServiceBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Updated Result REST Controller. Calls mastery update + result API
 * @author Jonghyun Seong
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v0")
public class UpdatedResultControllerV0 {

    @Autowired
    @Qualifier("MasteryServiceV0")
    private MasteryServiceBase masterySvc;

    @Autowired
    @Qualifier("ResultServiceV0")
    private ResultServiceBase resultSvc;
    
    @PutMapping("/updatedresult")
    ResponseEntity<Object> putUpdatedResult(@RequestHeader("token") String token, @RequestBody ProblemSolveListDTO probSolveList){
        String userID = JWTUtil.getJWTPayloadField(token, "userID");

        System.out.println(probSolveList.toString());


        //Step 1: update mastery
        try {
            masterySvc.updateMastery(userID, probSolveList.getProbIdList(), probSolveList.getCorrectList());            
        }
        catch (Throwable e){
            throw new GenericInternalException("ERR-0002", "Mastery update Exception: " + StackPrinter.getStackTrace(e));
        }

        //Step 2:
        DiagnosisResultDTO output = null;
        try {
            output = resultSvc.getResultOfUser(userID);
        }
        catch (Throwable e){
            throw new GenericInternalException("ERR-0003", "Result get exception: " + StackPrinter.getStackTrace(e));
        }

        return new ResponseEntity<>(output, HttpStatus.OK);
    }    
}
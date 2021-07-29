package com.tmax.WaplMath.AnalysisReport.controller.result;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultV1DTO;
import com.tmax.WaplMath.AnalysisReport.service.result.ResultServiceBaseV1;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Common.util.exception.StackPrinter;
import com.tmax.WaplMath.Recommend.service.mastery.v1.MasteryServiceBaseV1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Updated Result REST Controller. Calls mastery update + result API
 * @author Jonghyun Seong
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v1")
public class UpdatedResultControllerV1 {

    @Autowired
    @Qualifier("MasteryServiceV1")
    private MasteryServiceBaseV1 masterySvc;

    @Autowired
    @Qualifier("ResultServiceV1")
    private ResultServiceBaseV1 resultSvc;
    
    @PutMapping("/updatedresult")
    ResponseEntity<Object> putUpdatedResult(@RequestHeader("token") String token){
        String userID = JWTUtil.getUserID(token);

        //Step 1: update mastery => throw in mastery svc
        masterySvc.updateMasteryFromLRS(token);

        //Step 2:
        DiagnosisResultV1DTO output = null;
        try {
            output = resultSvc.getResultOfUserV1(userID);
        }
        catch (Throwable e){
            throw new GenericInternalException(ARErrorCode.RESULT_SERVICE_ERROR, StackPrinter.getStackTrace(e));
        }

        return new ResponseEntity<>(output, HttpStatus.OK);
    }    
}
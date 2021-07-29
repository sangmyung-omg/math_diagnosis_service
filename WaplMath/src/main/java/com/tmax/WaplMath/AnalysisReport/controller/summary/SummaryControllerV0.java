package com.tmax.WaplMath.AnalysisReport.controller.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.service.summary.SummaryServiceBase;
import com.tmax.WaplMath.Common.exception.InvalidTokenException;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

/**
 * Summary REST Controller
 * @author Jonghyun Seong
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path= ARConstants.apiPrefix + "/v0")
class SummaryControllerV0 {
    
    @Autowired
    @Qualifier("SummaryServiceV1")
    private SummaryServiceBase summarySvc;

    @GetMapping("/summary")
    ResponseEntity<Object> getSummary(@RequestHeader(value="token", required = false) String token) {
        if(token == null)  {
            throw new InvalidTokenException();
        }


        String userID  = JWTUtil.getUserID(token);        

        return new ResponseEntity<>(summarySvc.getSummaryOfUser(userID),HttpStatus.OK);
    }

    // @ExceptionHandler(GenericInternalException.class)
    // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    // public ResponseEntity<Object> handleInternalError(GenericInternalException exception){
    //     return new ResponseEntity<>(new Generic500ErrorDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
    // }
}
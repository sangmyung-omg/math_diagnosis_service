package com.tmax.WaplMath.AnalysisReport.controller.curriculum;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumDataDTO;
import com.tmax.WaplMath.AnalysisReport.service.curriculum.CurriculumServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path=ARConstants.apiPrefix + "/v2")
public class CurriculumControllerV2 {
    
    @Autowired
    CurriculumServiceBase currSvc;

    @PostMapping("/curriculum")
    public ResponseEntity<Object> getCurriculumList(@RequestHeader("token") String token,
                                                    @RequestParam(name="order", defaultValue = "seq") String order,
                                                    @RequestParam(name="exclude", defaultValue = "") String exclude,
                                                    @RequestBody List<String> currIDList) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");
        
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        CurriculumDataDTO result =  currSvc.getByIdList(userID, currIDList, excludeSet);

        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @GetMapping("/curriculum/{currID}")
    public ResponseEntity<Object> getSpecificCurriculum(@RequestHeader("token") String token,
                                                        @RequestParam(name="exclude", defaultValue = "") String exclude,
                                                        @PathVariable(name="currID") String currID) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");

        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));
        
        CurriculumDataDTO result =  currSvc.getByIdList(userID, Arrays.asList(currID), excludeSet);


        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @GetMapping("/curriculumquery")
    public ResponseEntity<Object> queryCurriculum(  @RequestHeader("token") String token,
                                                    @RequestParam("searchTerm") String searchTerm,
                                                    @RequestParam(name="exclude", defaultValue = "") String exclude,
                                                    @RequestParam(name="mode", required =  false) String mode,
                                                    @RequestParam(name="range", required = false) String range,
                                                    @RequestParam(name="typeRange", defaultValue = "") String typeRange,
                                                    @RequestParam(name="subsearch", defaultValue = "false") boolean subSearch,
                                                    @RequestParam(name="order", defaultValue = "id") String order) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");             
        
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));
        
        CurriculumDataDTO result = currSvc.searchWithConditions(userID, searchTerm, typeRange, mode, range, subSearch, order, excludeSet);

        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @GetMapping("/curriculumquery/year")
    public ResponseEntity<Object> queryCurriculumByYear(@RequestHeader("token") String token,
                                                        @RequestParam(name="exclude", defaultValue = "") String exclude,
                                                        @RequestParam(name="schoolType", defaultValue =  "mid") String schoolType,
                                                        @RequestParam(name="year", defaultValue = "") String year,
                                                        @RequestParam(name="typeRange", defaultValue = "") String typeRange,
                                                        @RequestParam(name="order", defaultValue = "id") String order) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");               
        
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        CurriculumDataDTO result = currSvc.searchByYear(userID, schoolType, year, typeRange, order, excludeSet);

        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @GetMapping("/curriculumquery/recent")
    public ResponseEntity<Object> queryRecentCurriculum(@RequestHeader("token") String token,
                                                        @RequestParam(name="exclude", defaultValue = "") String exclude,
                                                        @RequestParam(name="count", defaultValue =  "1000") Integer count,
                                                        @RequestParam(name="castto", defaultValue = "") String castTo,
                                                        @RequestParam(name="order", defaultValue = "id") String order) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");               

        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));                                                    
        
        CurriculumDataDTO result = currSvc.searchRecent(userID, count, castTo, order, excludeSet);

        return new ResponseEntity<>(result,HttpStatus.OK);
    }
}

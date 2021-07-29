package com.tmax.WaplMath.AnalysisReport.controller.userdata;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserMasteryDataListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserStudyDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserStudyDataListRequestDTO;
import com.tmax.WaplMath.AnalysisReport.service.userdata.UserDataServiceBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v2")
public class UserDataControllerV2 {
    @Autowired
    UserDataServiceBase userDataSvc;

    @PostMapping("/userdata/ukknowledge")
    public ResponseEntity<Object> getStudyStats(@RequestHeader("token") String token, 
                                                @RequestBody List<String> userIDList,
                                                @RequestParam(name="exclude", defaultValue = "") String exclude) {
        
        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        //remove duplicates from userID List
        Set<String> userIDSet = new HashSet<>(userIDList);
        
        UserMasteryDataListDTO result = userDataSvc.getUserMasteryDataList(new ArrayList<>(userIDSet), excludeSet);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/userdata/studystat")
    public ResponseEntity<Object> getUserUKKnowledge(@RequestHeader("token") String token,
                                                     @RequestBody UserStudyDataListRequestDTO reqBody,
                                                     @RequestParam(name="exclude", defaultValue = "") String exclude) {
        // String userID  = JWTUtil.getUserID(token);

        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        //Dummy data
        List<String> userIDList = reqBody.getUserIDList(); 

        //remove duplicates from userID List
        Set<String> userIDSet = new HashSet<>(userIDList);

        List<UserStudyDataDTO> result = userDataSvc.getStudyStatList(new ArrayList<>(userIDSet), excludeSet);
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

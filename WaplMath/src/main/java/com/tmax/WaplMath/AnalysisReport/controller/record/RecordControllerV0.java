package com.tmax.WaplMath.AnalysisReport.controller.record;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.LevelDiagnosisRecordDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;
import com.tmax.WaplMath.AnalysisReport.service.record.RecordServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.auth.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class RecordControllerV0 {

    @Autowired
    @Qualifier("RecordServiceV0")
    private RecordServiceBase recordSvc;
       
    @GetMapping("/record")
    ResponseEntity<Object> getLevelDiagRecord(@RequestHeader("token") String token){
        //TODO: get userID from token data
        String userID  = JWTUtil.getJWTPayloadField(token, "userID");

        LevelDiagnosisRecordDTO output = recordSvc.getRecordOfUser(userID);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping("/record/{userID}")
    ResponseEntity<Object> getUserIDLevelDiagRecord(@RequestHeader("token") String token, @PathVariable("userID") String userID){
        LevelDiagnosisRecordDTO output = recordSvc.getRecordOfUser(userID);

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @PostMapping("/records")
    ResponseEntity<Object> getListLevelDiagRecords(@RequestHeader("token") String token, @RequestBody UserIDListDTO userIDList){
        List<LevelDiagnosisRecordDTO> outputList = recordSvc.getRecordOfUserList(userIDList);

        return new ResponseEntity<>(outputList, HttpStatus.OK);
    }
}

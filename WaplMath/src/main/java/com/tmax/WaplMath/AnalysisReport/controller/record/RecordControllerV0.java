package com.tmax.WaplMath.AnalysisReport.controller.record;

import java.util.ArrayList;
import java.util.List;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.LevelDiagnosisRecordDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class RecordControllerV0 {
    
    @GetMapping("/record")
    LevelDiagnosisRecordDTO getLevelDiagRecord(@RequestHeader("token") String token){
        LevelDiagnosisRecordDTO output = new LevelDiagnosisRecordDTO();
        output.setNumCorrect(10);
        output.setNumDontknow(12);
        output.setNumWrong(9);
        output.setTimeConsumed(300);

        return output;
    }

    @GetMapping("/record/{userID}")
    LevelDiagnosisRecordDTO getUserIDLevelDiagRecord(@RequestHeader("token") String token, @PathVariable("userID") String userID){
        LevelDiagnosisRecordDTO output = new LevelDiagnosisRecordDTO();
        output.setNumCorrect(10 + userID.length());
        output.setNumDontknow(12 + userID.length());
        output.setNumWrong(9 + userID.length());
        output.setTimeConsumed(300 + userID.length());

        return output;
    }

    @PostMapping("/records")
    List<LevelDiagnosisRecordDTO> getListLevelDiagRecords(@RequestHeader("token") String token, @RequestBody UserIDListDTO userIDList){
        List<LevelDiagnosisRecordDTO> outputList = new ArrayList<LevelDiagnosisRecordDTO>();
        
        for (String userID : userIDList.getUserIDList()) {
            LevelDiagnosisRecordDTO levelData = new LevelDiagnosisRecordDTO();
            levelData.setNumCorrect(10 + userID.length());
            levelData.setNumDontknow(12 + userID.length());
            levelData.setNumWrong(9 + userID.length());
            levelData.setTimeConsumed(300 + userID.length());
            outputList.add(levelData);
        }


        return outputList;
    }
}

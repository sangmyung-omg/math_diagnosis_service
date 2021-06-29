package com.tmax.WaplMath.AnalysisReport.service.record;

import java.util.ArrayList;
import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.LevelDiagnosisRecordDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;

import org.springframework.stereotype.Service;

/**
 * Record service dummy data implmentation
 * @author Jonghyun Seong
 */
@Service("RecordServiceDummy")
public class RecordServiceDummy implements RecordServiceBase{
    @Override
    public LevelDiagnosisRecordDTO getRecordOfUser(String userID) {
        // Dummy svc
        LevelDiagnosisRecordDTO output = new LevelDiagnosisRecordDTO();
        output.setNumCorrect(10 + userID.length());
        output.setNumDontknow(12 + userID.length());
        output.setNumWrong(9 + userID.length());
        output.setTimeConsumed(300 + userID.length());

        return output;
    }

    @Override
    public List<LevelDiagnosisRecordDTO> getRecordOfUserList(UserIDListDTO userIDList) {
        List<LevelDiagnosisRecordDTO> outputList = new ArrayList<LevelDiagnosisRecordDTO>();
        
        for (String userID : userIDList.getUserIDList()) {
            outputList.add(this.getRecordOfUser(userID));
        }

        return outputList;
    }
}

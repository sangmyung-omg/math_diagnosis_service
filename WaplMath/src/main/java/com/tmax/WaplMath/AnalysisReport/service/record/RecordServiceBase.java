package com.tmax.WaplMath.AnalysisReport.service.record;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.LevelDiagnosisRecordDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;

public interface RecordServiceBase {
    LevelDiagnosisRecordDTO getRecordOfUser(String userID);
    List<LevelDiagnosisRecordDTO> getRecordOfUserList(UserIDListDTO userIDList);
}

package com.tmax.WaplMath.AnalysisReport.service.result;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;

public interface ResultServiceBase {
    DiagnosisResultDTO getResultOfUser(String userID);
    List<DiagnosisResultDTO> getResultOfMultipleUsers(UserIDListDTO userIDList);
}

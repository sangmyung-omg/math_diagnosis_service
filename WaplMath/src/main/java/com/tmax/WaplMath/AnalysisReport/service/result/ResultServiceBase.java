package com.tmax.WaplMath.AnalysisReport.service.result;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultDTO;

/**
 * Result service base interface
 * @author Jonghyun Seong
 */
public interface ResultServiceBase {
    DiagnosisResultDTO getResultOfUser(String userID);
    List<DiagnosisResultDTO> getResultOfMultipleUsers(UserIDListDTO userIDList);
}

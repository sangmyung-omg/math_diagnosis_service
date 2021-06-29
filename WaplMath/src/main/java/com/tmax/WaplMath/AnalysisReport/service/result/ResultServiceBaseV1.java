package com.tmax.WaplMath.AnalysisReport.service.result;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultV1DTO;

/**
 * Result service base interface
 * @author Jonghyun Seong
 */
public interface ResultServiceBaseV1 extends ResultServiceBase{
    DiagnosisResultV1DTO getResultOfUserV1(String userID);
    List<DiagnosisResultV1DTO> getResultOfMultipleUsersV1(UserIDListDTO userIDList);
}

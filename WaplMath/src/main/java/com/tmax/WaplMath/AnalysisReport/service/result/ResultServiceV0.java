package com.tmax.WaplMath.AnalysisReport.service.result;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("v0")
public class ResultServiceV0 implements ResultServiceBase{
    @Override
    public List<DiagnosisResultDTO> getResultOfMultipleUsers(UserIDListDTO userIDList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DiagnosisResultDTO getResultOfUser(String userID) {
        // TODO Auto-generated method stub
        return null;
    }
}

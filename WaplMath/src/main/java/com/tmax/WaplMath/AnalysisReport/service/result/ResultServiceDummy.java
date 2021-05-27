package com.tmax.WaplMath.AnalysisReport.service.result;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;
import com.tmax.WaplMath.AnalysisReport.service.record.RecordServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.summary.SummaryServiceBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ResultServiceDummy")
public class ResultServiceDummy implements ResultServiceBase{
    @Autowired
    private SummaryServiceBase summarySvc;

    @Autowired
    private RecordServiceBase recordSvc;


    @Override
    public List<DiagnosisResultDTO> getResultOfMultipleUsers(UserIDListDTO userIDList) {
        List<DiagnosisResultDTO> resultList = new ArrayList<DiagnosisResultDTO>();
        for(int i=0; i < userIDList.getUserIDList().size(); i++){
            resultList.add(this.getResultOfUser(""));
        }
        return resultList;
    }

    @Override
    public DiagnosisResultDTO getResultOfUser(String userID) {
        DiagnosisResultDTO resultData = new DiagnosisResultDTO();

        resultData.setSummary(summarySvc.getSummaryOfUser(userID));
        resultData.setLevelDiagnosisRecord(recordSvc.getRecordOfUser(userID));


        ChapterIDListDTO chapterIDList = new ChapterIDListDTO();
        List<String> idList = new ArrayList<String>();
        for(int i=0; i<5; i++){
            idList.add(UUID.randomUUID().toString());
        }
        chapterIDList.setUserIDList(idList);

        resultData.setChapterIDList(chapterIDList);

        return resultData;
    }
}

package com.tmax.WaplMath.AnalysisReport.service.result;

import java.util.ArrayList;
import java.util.List;
// import java.util.UUID;

import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.record.RecordServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.summary.SummaryServiceBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Result service V0 implementation
 * @author Jonghyun Seong
 */
@Service("ResultServiceV0")
@Primary
public class ResultServiceV0 implements ResultServiceBase{
    @Autowired
    private SummaryServiceBase summarySvc;

    @Autowired
    private RecordServiceBase recordSvc;

    @Autowired
    private ChapterServiceBase chapterSvc;


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
        resultData.setChapterDetailList(chapterSvc.getAllChapterListOfUserChapterOnly(userID));

        

        return resultData;
    }
}

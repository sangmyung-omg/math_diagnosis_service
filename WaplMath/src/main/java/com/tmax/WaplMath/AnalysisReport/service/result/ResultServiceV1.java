package com.tmax.WaplMath.AnalysisReport.service.result;

import java.util.ArrayList;
import java.util.List;
// import java.util.UUID;

import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultDTO;
import com.tmax.WaplMath.AnalysisReport.dto.result.DiagnosisResultV1DTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.record.RecordServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreServiceV0;
import com.tmax.WaplMath.AnalysisReport.service.summary.SummaryServiceBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Result service V0 implementation
 * @author Jonghyun Seong
 */
@Service("ResultServiceV1")
@Primary
public class ResultServiceV1 implements ResultServiceBaseV1 {
    @Autowired
    @Qualifier("SummaryServiceV1")
    private SummaryServiceBase summarySvc;

    @Autowired
    private RecordServiceBase recordSvc;

    @Autowired
    @Qualifier("ChapterServiceV1")
    private ChapterServiceBase chapterSvc;

    @Autowired
    private WaplScoreServiceV0 waplScoreSvc;


    //Service for reverse compat
    @Autowired
    private ResultServiceV0 resultSvcV0;


    @Override
    public List<DiagnosisResultDTO> getResultOfMultipleUsers(UserIDListDTO userIDList) {
        return resultSvcV0.getResultOfMultipleUsers(userIDList);
    }

    @Override
    public DiagnosisResultDTO getResultOfUser(String userID) {
        return resultSvcV0.getResultOfUser(userID);
    }

    //V1 API
    @Override
    public DiagnosisResultV1DTO getResultOfUserV1(String userID) {
        DiagnosisResultV1DTO resultData = new DiagnosisResultV1DTO();
        
        resultData.setSummary(summarySvc.getSummaryOfUser(userID));
        resultData.setLevelDiagnosisRecord(recordSvc.getRecordOfUser(userID));
        resultData.setChapterDetailList(chapterSvc.getAllChapterListOfUserChapterOnly(userID));
        resultData.setWaplScore(WAPLScoreDTO.getScaledScore(waplScoreSvc.getWaplScore(userID)));

        return resultData;
    }

    @Override
    public List<DiagnosisResultV1DTO> getResultOfMultipleUsersV1(UserIDListDTO userIDList) {
        List<DiagnosisResultV1DTO> resultList = new ArrayList<>();
        for(String userID : userIDList.getUserIDList()){
            resultList.add(this.getResultOfUserV1(userID));
        }
        return resultList;
    }
}

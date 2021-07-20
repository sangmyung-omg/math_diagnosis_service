package com.tmax.WaplMath.AnalysisReport.service.report;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumSimpleDTO;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataLiteDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.service.curriculum.CurriculumServiceV0;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.score.ScoreServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
@Service("AR-ReportServiceV0")
public class ReportServiceV0 implements ReportServiceBaseV0{
    @Autowired
    ScoreServiceBase scoreSvc;

    @Autowired
    CurriculumServiceV0 currSvc;

    @Autowired
    UserStatisticsServiceBase userStatSvc;

    @Autowired
    CurriculumRepository currRepo;

    @Override
    public ReportDataDTO getReport(String userID, Set<String> excludeSet) {
        //Get basic score data
        PersonalScoreDTO score = excludeSet.contains("score") ? null :  scoreSvc.getUserScore(userID, excludeSet);
        PersonalScoreDTO waplscore = excludeSet.contains("waplscore") ? null :  scoreSvc.getUserScore(userID, excludeSet);
        PersonalScoreDTO targetscore = excludeSet.contains("targetscore") ? null :  scoreSvc.getUserScore(userID, excludeSet);
        GlobalStatisticDTO stats = excludeSet.contains("stats") ? null :  scoreSvc.getScoreStats(userID, excludeSet, 100);


        // CurriculumDataDTO currData = currSvc.getByIdList(userID, currIDList, excludeSet);
        CurriculumDataDTO currData = null;
        if(!excludeSet.contains("currData")){
            //List<String> currIDList = getRecentCurrIDList(userID, 10);
            //currData = currSvc.getByIdList(userID, currIDList, excludeSet);
            currData = currSvc.searchRecent(userID, 1000, "chapter", "", excludeSet);
        }
        
        return ReportDataDTO.builder()
                            .score(score)
                            .waplscore(waplscore)
                            .targetscore(targetscore)
                            .stats(stats)
                            .currData(currData)
                            .build();
    }

    @Override
    public ReportDataLiteDTO getReportLite(String userID, Set<String> excludeSet) {
        //Get basic score data
        PersonalScoreDTO score = excludeSet.contains("score") ? null :  scoreSvc.getUserScore(userID, excludeSet);
        PersonalScoreDTO waplscore = excludeSet.contains("waplscore") ? null :  scoreSvc.getUserScore(userID, excludeSet);
        PersonalScoreDTO targetscore = excludeSet.contains("targetscore") ? null :  scoreSvc.getUserScore(userID, excludeSet);
        GlobalStatisticDTO stats = excludeSet.contains("stats") ? null :  scoreSvc.getScoreStats(userID, excludeSet, 100);


        List<CurriculumSimpleDTO> currList = null;
        if(!excludeSet.contains("currData")){
            List<String> currIDList = getRecentCurrIDList(userID, 10);
            List<Curriculum> resultList= (List<Curriculum>)currRepo.findAllById(currIDList);
            currList = resultList.stream().map(curr -> {
                String type = currSvc.getCurriculumType(curr);
                return CurriculumSimpleDTO.builder()
                                          .id(curr.getCurriculumId())
                                          .seq(curr.getCurriculumSequence())
                                          .name(currSvc.getName(curr, type))
                                          .type(type)
                                          .build();
            }).collect(Collectors.toList());
        }



        return ReportDataLiteDTO.builder()
                                .score(score)
                                .waplscore(waplscore)
                                .targetscore(targetscore)
                                .stats(stats)
                                .currList(currList)
                                .build();   
    }

    private List<String> getRecentCurrIDList(String userID, int count){
        Statistics recentCurrStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_RECENT_CURR_ID_LIST);
        if(recentCurrStat == null){
            return null;
        }

        List<String> recentList = recentCurrStat.getAsStringList();
        int index = Math.max(0, recentList.size() - count);

        return recentList.subList(index, recentList.size());
    }
}
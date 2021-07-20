package com.tmax.WaplMath.AnalysisReport.service.statistics.score;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.CorrectRateDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.SolveSpeedDTO;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.WaplScoreServiceBaseV0;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.AnalysisReport.util.statistics.StatisticsUtil;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.InvalidArgumentException;
import com.tmax.WaplMath.Common.exception.UserNotFoundException;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.UserRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("ScoreServiceV0")
public class ScoreServiceV0 implements ScoreServiceBase {

    @Autowired
    @Qualifier("UserStatisticsServiceV0")
    private UserStatisticsServiceBase userStatSvc;

    @Autowired
    @Qualifier("CurrStatisticsServiceV0")
    private CurrStatisticsServiceBase currStatSvc;

    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Autowired
    @Qualifier("AR-WaplScoreServiceV0")
    WaplScoreServiceBaseV0 waplScoreSvc;


    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ExamScopeUtil examScopeUtil;

    @Override
    public PersonalScoreDTO getUserScore(String userID, Set<String> excludeList) {
        //Exception handling for input parameters
        if(userID == null){
            throw new InvalidArgumentException("userID is null");
        }

        //Get score only when not excluded
        Float score = null;
        if(!excludeList.contains("score")){
            //Check if examscore exists
            Statistics userScore = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_EXAMSCOPE_SCORE);

            if(userScore == null){
                log.error("Exam score not found for user " + userID);
                throw new GenericInternalException(ARErrorCode.GENERIC_ERROR, "Exam score not found");
            }

            try {
                score = userScore.getAsFloat();
            }
            catch(Throwable e){
                log.error("Examscope cannot be converted to float." + userID + userScore.toString());
            }
        }

        //Get percentile when not excluded, and if score is not null
        Float percentile = null;
        if(!excludeList.contains("percentile") && score != null){
            //Check if examscore lut exists
            Statistics examscoreLUT = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_EXAMSCOPE_PERCENTILE_LUT);

            //If null. generate new one. TODO --> create a service that does this by user exam scope change event
            List<Float> masteryList = null;
            if(examscoreLUT == null){
                masteryList = generatePercentileLUT(userID);
            }
            else {
                try {
                    masteryList = examscoreLUT.getAsFloatList();
                }
                catch(Throwable e){
                    log.error("Examscope LUT cannot be converted to float." + userID + examscoreLUT.toString());
                }
            }

            percentile = ukStatSvc.getPercentile(score, masteryList);
        }


        
        return PersonalScoreDTO.builder()
                               .score(100*score)
                               .percentile(100*percentile)
                               .build();
    }

    @Override
    public PersonalScoreDTO getWaplScore(String userID, Set<String> excludeList) {
        //Exception handling for input parameters
        if(userID == null){
            throw new InvalidArgumentException("userID is null");
        }

        //Get score only when not excluded
        Float score = null;
        if(!excludeList.contains("score")){
            //Check if examscore exists
            Statistics waplscore = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_WAPL_SCORE);

            if(waplscore == null){
                //This invokes generation of waplscore if not exist
                score = waplScoreSvc.getWaplScore(userID).getScore();
            }
            else{
                try {
                    score = waplscore.getAsFloat();
                }
                catch(Throwable e){
                    log.error("Examscope cannot be converted to float." + userID + waplscore.toString());
                }
            }
        }

        //Get percentile when not excluded, and if score is not null
        Float percentile = null;
        if(!excludeList.contains("percentile") && score != null){
            //Check if examscore lut exists
            Statistics examscoreLUT = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_EXAMSCOPE_PERCENTILE_LUT);

            //If null. generate new one. TODO --> create a service that does this by user exam scope change event
            List<Float> masteryList = null;
            if(examscoreLUT == null){
                masteryList = generatePercentileLUT(userID);
            }
            else {
                try {
                    masteryList = examscoreLUT.getAsFloatList();
                }
                catch(Throwable e){
                    log.error("Examscope LUT cannot be converted to float." + userID + examscoreLUT.toString());
                }
            }

            percentile = ukStatSvc.getPercentile(score, masteryList);
        }


        
        return PersonalScoreDTO.builder()
                               .score(100*score)
                               .percentile(100*percentile)
                               .build();
    }

    @Override
    public PersonalScoreDTO getTargetScore(String userID, Set<String> excludeList) {
        //Exception handling for input parameters
        if(userID == null){
            throw new InvalidArgumentException("userID is null");
        }

        //Get score only when not excluded
        Float score = null;
        if(!excludeList.contains("score")){
            //Get userInfo and get target score
            Optional<User> userInfo = userRepo.findById(userID);
            if(userInfo.isPresent()){
                score = (float)userInfo.get().getExamTargetScore();
            }
        }

        //Get percentile when not excluded, and if score is not null
        Float percentile = null;
        if(!excludeList.contains("percentile") && score != null){
            //Check if examscore lut exists
            Statistics examscoreLUT = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_EXAMSCOPE_PERCENTILE_LUT);

            //If null. generate new one. TODO --> create a service that does this by user exam scope change event
            List<Float> masteryList = null;
            if(examscoreLUT == null){
                masteryList = generatePercentileLUT(userID);
            }
            else {
                try {
                    masteryList = examscoreLUT.getAsFloatList();
                }
                catch(Throwable e){
                    log.error("Examscope LUT cannot be converted to float." + userID + examscoreLUT.toString());
                }
            }

            percentile = 100* ukStatSvc.getPercentile(score / 100.0f, masteryList);
        }


        
        return PersonalScoreDTO.builder()
                               .score(score)
                               .percentile(percentile)
                               .build();
    }

    @Override
    public GlobalStatisticDTO getScoreStats(String userID, Set<String> excludeList, int histogramSize) {
        //Get all grade statistics
        Optional<User> userInfo = userRepo.findById(userID);
        if(!userInfo.isPresent()){
            throw new UserNotFoundException();
        }

        //Get grade
        String grade = userInfo.get().getGrade();
        String currentCurrID = userInfo.get().getCurrentCurriculumId();
        List<String> examScopeCurrIDList = examScopeUtil.getCurrIdListOfScope(userID);

        Float mean = null;
        if(!excludeList.contains("mean")){
            Statistics meanStat = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList,
                                                                         CurrStatisticsServiceBase.STAT_MASTERY_MEAN + "_grade_" + grade);
            if(meanStat != null)
                mean = meanStat.getAsFloat();
        }

        //Total count of users
        Integer totalCnt = null;

        //TODO: not supported yet
        Float median = null;
        if(!excludeList.contains("median")){
            Statistics medianStat = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList,
                                                                           CurrStatisticsServiceBase.STAT_MASTERY_MEDIAN + "_grade_" + grade);
            if(medianStat != null)
                median = medianStat.getAsFloat();
        }

        Float std = null;
        if(!excludeList.contains("std")){
            Statistics stdStat = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList,
                                                                        CurrStatisticsServiceBase.STAT_MASTERY_STD + "_grade_" + grade);
            if(stdStat != null)
                std = stdStat.getAsFloat();
        }

        List<Integer> histogram = null;
        if(!excludeList.contains("histogram")){
            Statistics masteryList = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList,
                                                                            CurrStatisticsServiceBase.STAT_MASTERY_PERCENTILE_LUT+ "_grade_" + grade);
            //Make histogram
            if(masteryList != null){
                //Create slots
                histogram = new ArrayList<>(Collections.nCopies(histogramSize, 0));
                float step = 1.0f / (float)histogramSize;
                for(Float mastery : masteryList.getAsFloatList()){
                    int idx = Math.min((int) Math.floor(mastery / step), histogramSize - 1);
                    histogram.set(idx, histogram.get(idx) + 1);
                }

                totalCnt = masteryList.getAsFloatList().size();
            }
        }

        List<Float> percentile = null;
        if(!excludeList.contains("percentile")){
            //Statistics percentileLUTStat = currStatSvc.getStatistics(currentCurrID ,CurrStatisticsServiceBase.STAT_MASTERY_PERCENTILE_LUT+ "_grade_" + grade);

            //TODO fix: this LUT is not precise. must fix
            Statistics percentileLUTStat = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList,
                                                                            CurrStatisticsServiceBase.STAT_MASTERY_PERCENTILE_LUT+ "_grade_" + grade);
            if(percentileLUTStat != null){
                List<Float> percentileLUT = percentileLUTStat.getAsFloatList();

                //resize to 101; TODO. potential short list error
                percentile = StatisticsUtil.createPercentileLUT(percentileLUT, 101).stream().map(data -> 100*data).collect(Collectors.toList());
            }
        }

        //TODO: Fix this dumb thing
        if(!excludeList.contains("totalCnt")){
            if(totalCnt == null)
                totalCnt = userRepo.getByGrade(grade).size();
        }
        else{
            totalCnt = null;
        }

        return GlobalStatisticDTO.builder()
                                 .mean(mean)
                                 .median(median)
                                 .std(std)
                                 .histogram(histogram)
                                 .percentile(percentile)
                                 .totalCnt(totalCnt)
                                 .build();
    }


    @Override
    public CorrectRateDTO getCorrectRate(String userID, Set<String> excludeSet) {
        //Get from stat
        Float correctrate = null;
        if(!excludeSet.contains("correctrate")){
            Statistics correctRateStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_CORRECT_RATE);
            if(correctRateStat != null){
                correctrate = correctRateStat.getAsFloat();
            }
        }

        
        Integer problemcount = null;
        if(!excludeSet.contains("problemcount")){
            Statistics countStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_RATE_PROBLEM_COUNT);
            if(countStat != null){
                problemcount = countStat.getAsInt();
            }
        }

        return CorrectRateDTO.builder()
                             .correctrate(correctrate)
                             .problemcount(problemcount)
                             .build();
    }

    @Override
    public SolveSpeedDTO getSolveSpeedRate(String userID, Set<String> excludeSet) {
        //Get from stat
        Float satisfyRate = null;
        if(!excludeSet.contains("correctrate")){
            Statistics satisfyRateStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_SOLVING_SPEED_SATISFY_RATE);
            if(satisfyRateStat != null){
                satisfyRate = satisfyRateStat.getAsFloat();
            }
        }

        
        Integer problemcount = null;
        if(!excludeSet.contains("problemcount")){
            Statistics countStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_RATE_PROBLEM_COUNT);
            if(countStat != null){
                problemcount = countStat.getAsInt();
            }
        }

        return SolveSpeedDTO.builder()
                            .satisfyRate(satisfyRate)
                            .problemcount(problemcount)
                            .build();
    }

    private List<Float> generatePercentileLUT(String userID) {
        //Get usergrade from info
        Optional<User> userInfo = userRepo.findById(userID);
        if(!userInfo.isPresent()){
            throw new UserNotFoundException();
        }

        String grade = userInfo.get().getGrade();

        List<String> examScopeCurrIDList = examScopeUtil.getCurrIdListOfScope(userID);
        List<Float> masteryList = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList, 
                                                            CurrStatisticsServiceBase.STAT_MASTERY_PERCENTILE_LUT + "_grade_" + grade).getAsFloatList();

        //Save for future use
        userStatSvc.updateCustomUserStat(userID, 
                                        UserStatisticsServiceBase.STAT_EXAMSCOPE_PERCENTILE_LUT, 
                                        Statistics.Type.FLOAT_LIST, 
                                        masteryList.toString());

        return masteryList;
    }
}

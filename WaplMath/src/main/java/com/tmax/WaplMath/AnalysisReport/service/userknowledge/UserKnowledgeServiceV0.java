package com.tmax.WaplMath.AnalysisReport.service.userknowledge;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.HashSet;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.uk.UkSimpleDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeDetailDTO;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.UserNotFoundException;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledgeKey;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.Common.repository.uk.UkRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;

@Slf4j
@Service("AR-UserKnowledgeServiceV0")
public class UserKnowledgeServiceV0 implements UserKnowledgeServiceBase {
    @Autowired
    UkRepo ukRepo;

    @Autowired
    UserKnowledgeRepo userKnowledgeRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CurrStatisticsServiceBase currStatSvc;

    @Autowired
    UKStatisticsServiceBase ukStatSvc;

    @Autowired
    UserStatisticsServiceBase userStatSvc;

    @Override
    public UkUserKnowledgeDetailDTO getByUkId(String userID, Integer ukID, Set<String> excludeSet) {
        //Get UK info
        Optional<Uk> ukOpt = ukRepo.findById(ukID);
        if(!ukOpt.isPresent()){
            log.warn("uk ID does not exist. "+ ukID);
            throw new GenericInternalException(ARErrorCode.INVALID_PARAMETER, "Uk ID is invalid.");
        }

        Uk uk = ukOpt.get();
        
        return UkUserKnowledgeDetailDTO.builder()
                                       .ukInfo(getUkInfo(uk))
                                       .mastery(!excludeSet.contains("mastery") ? getMastery(userID, uk) : null)
                                       .stats(!excludeSet.contains("stats") ? getStats(uk) : null)
                                       .waplscore(!excludeSet.contains("waplscore") ? getWaplScore(userID, uk) : null)
                                       .build();
    }
    
    @Override
    public UkUserKnowledgeDetailDTO getByUkId(String userID, Integer ukID) {
        return getByUkId(userID, ukID, new HashSet<>());
    }

    @Override
    public List<UkUserKnowledgeDetailDTO> getByUkIdList(String userID, List<Integer> ukID) {
        return getByUkIdList(userID, ukID, new HashSet<>());
    }

    @Override
    public List<UkUserKnowledgeDetailDTO> getByUkIdList(String userID, List<Integer> ukIDList, Set<String> excludeSet) {
        return ukIDList.stream().parallel().map(ukID -> getByUkId(userID, ukID, excludeSet)).collect(Collectors.toList());
    }

    private UkSimpleDTO getUkInfo(Uk uk){
        return UkSimpleDTO.builder()
                          .name(uk.getUkName())
                          .id(uk.getUkId())
                          .build();
    }

    private PersonalScoreDTO getMastery(String userID, Uk uk){
        Optional<UserKnowledge> uknowOpt = userKnowledgeRepo.findById(new UserKnowledgeKey(userID, uk.getUkId()));
        if(!uknowOpt.isPresent()){
            log.warn("No knowledge found for user "+ userID);
            return new PersonalScoreDTO();
        }

        UserKnowledge uknow = uknowOpt.get();

        //Get percentile
        //Get grade
        Optional<User> userOpt = userRepo.findById(userID);
        if(!userOpt.isPresent()){
            log.error("No user info found " + userID);
            throw new UserNotFoundException();
        }

        // User user = userOpt.get();

        //Get sortedMastery TODO. make this percentile LUT format + apply grade
        Statistics sortedListStat = ukStatSvc.getUKStatistics(uk.getUkId(), UKStatisticsServiceBase.STAT_MASTERY_SORTED);
        Float percentile = null;
        if(sortedListStat != null)
            percentile = 100 * ukStatSvc.getPercentile(uknow.getUkMastery(), sortedListStat.getAsFloatList());



        return PersonalScoreDTO.builder()
                               .score(100 * uknow.getUkMastery())
                               .percentile(percentile)
                               .build();
    }

    private GlobalStatisticDTO getStats(Uk uk){
        Integer ukID = uk.getUkId();

        //Mean
        Statistics meanStat = ukStatSvc.getUKStatistics(ukID, UKStatisticsServiceBase.STAT_MASTERY_MEAN);
        Float mean = null;
        if(meanStat != null){
            mean = 100 * meanStat.getAsFloat();
        }

        //Median. TODO not supported yet
        Statistics medianStat = ukStatSvc.getUKStatistics(ukID, UKStatisticsServiceBase.STAT_MASTERY_MEDIAN);
        Float median = null;
        if(medianStat != null){
            median = 100 * medianStat.getAsFloat();
        }

        //std.
        Statistics stdStat = ukStatSvc.getUKStatistics(ukID, UKStatisticsServiceBase.STAT_MASTERY_STD);
        Float std = null;
        if(stdStat != null){
            std = 100 * stdStat.getAsFloat();
        }

        //Histogram + Total cnt
        Statistics sortedListStat = ukStatSvc.getUKStatistics(ukID, UKStatisticsServiceBase.STAT_MASTERY_SORTED);
        List<Integer> histogram = null;
        Integer totalCnt = 0;
        int histogramSize = 100;
        if(sortedListStat != null){
            //Create slots. TODO --> make histogram size as option
            histogram = new ArrayList<>(Collections.nCopies(histogramSize, 0));
            float step = 1.0f / (float)histogramSize;
            for(Float mastery : sortedListStat.getAsFloatList()){
                int idx = Math.min((int) Math.floor(mastery / step), histogramSize - 1);
                histogram.set(idx, histogram.get(idx) + 1);
            }

            totalCnt = sortedListStat.getAsFloatList().size();
        }


        return GlobalStatisticDTO.builder()
                                 .mean(mean)
                                 .median(median)
                                 .std(std)
                                 .histogram(histogram)
                                 .totalCnt(totalCnt)
                                 .build();
    }

    private PersonalScoreDTO getWaplScore(String userID, Uk uk){
        Statistics waplMasteryStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_WAPL_SCORE_MASTERY);
        if(waplMasteryStat == null){
            log.warn("No wapl score mastery for " + userID);
            return null;
        }

        //Make it to map
        Type type = new TypeToken<List<Map<String, Float>>>(){}.getType();
        List<Map<String, Float>> masteryMapList = new Gson().fromJson(waplMasteryStat.getData(), type);
        
        //mastery map size error
        if(masteryMapList.size() == 0){
            log.error("WAPL score mastery is invalid for {}. Type regeneration." , userID);
            return null;
        }            

        Float score = 100 * masteryMapList.get(masteryMapList.size() - 1).get(uk.getUkId().toString());

        //Get sortedMastery TODO. make this percentile LUT format + apply grade
        Statistics sortedListStat = ukStatSvc.getUKStatistics(uk.getUkId(), UKStatisticsServiceBase.STAT_MASTERY_SORTED);
        Float percentile = null;
        if(sortedListStat != null)
            percentile = 100 * ukStatSvc.getPercentile(score / 100.0f, sortedListStat.getAsFloatList());


        return PersonalScoreDTO.builder()
                               .score(score)
                               .percentile(percentile)
                               .build();
    }
}

package com.tmax.WaplMath.AnalysisReport.service.userdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.BasicProblemStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.CorrectRateDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.uk.UkDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserMasteryDataListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserStudyDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserUKKnowledgeDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeScoreDTO;
import com.tmax.WaplMath.AnalysisReport.service.statistics.score.ScoreServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceV0;
import com.tmax.WaplMath.AnalysisReport.service.userknowledge.UserKnowledgeServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.Common.repository.uk.UkRepo;
import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.BasicProblemStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.CustomStatDataDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("AR-UserDataServiceV0")
public class UserDataServiceV0 implements UserDataServiceBase {
    @Autowired
    UserKnowledgeServiceBase userKnowledgeSvc;

    @Autowired
    UserKnowledgeRepo userKnowledgeRepo;

    @Autowired
    UkRepo ukRepo;

    @Autowired
    ScoreServiceBase scoreSvc;

    @Autowired
    UserStatisticsServiceV0 userStatSvc;

    @Override
    public List<UserStudyDataDTO> getStudyStatList(List<String> userIDList, Set<String> excludeSet) {
        List<UserStudyDataDTO> result = 
            userIDList.stream()
                      .parallel()
                      .map(id -> {
                            int totalcnt = 0;
                            int correct = 0;
                            int wrong = 0;
                            int pass = 0;

                            try {
                                correct = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_CORRECT_CNT).getAsInt();
                                pass = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_PASS_CNT).getAsInt();
                                wrong = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_WRONG_CNT).getAsInt();
                                totalcnt = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_RATE_PROBLEM_COUNT).getAsInt();
                            }
                            catch (Throwable e){
                                log.error("Count Stats not found. fallback to old stat");
                                CorrectRateDTO correctRate = scoreSvc.getCorrectRate(id, excludeSet);
                                totalcnt = correctRate.getProblemcount();
                                correct = (int)(correctRate.getCorrectrate() * correctRate.getProblemcount());
                                wrong = correctRate.getProblemcount() - correct;
                            }

                            double random1 = Math.random();
                            double random2 = random1 * Math.random();
                            double random3 = (1-random1) * Math.random();

                            return UserStudyDataDTO.builder()
                                                .userID(id)
                                                .basic(BasicProblemStatDTO.builder()
                                                                        .correct(correct)
                                                                        .wrong(wrong)
                                                                        .pass(pass)
                                                                        .totalcnt(totalcnt)
                                                                        .build()
                                                                        )
                                                .custom(CustomStatDataDTO.builder()
                                                                         .pick((int)(totalcnt * random1))
                                                                         .unknown((int)(totalcnt * random2))
                                                                         .notfocused((int)(totalcnt * random3))
                                                                         .notserious((int)(totalcnt * (1-random3)))
                                                                         .build()
                                                                         )
                                                .build();
                }).collect(Collectors.toList());

        return result;
    }

    @Override
    public UserMasteryDataListDTO getUserMasteryDataList(List<String> userIDList, Set<String> excludeSet) {
        //Exception handling
        if(userIDList == null || userIDList.isEmpty()){
            throw new GenericInternalException(CommonErrorCode.INVALID_ARGUMENT, "No userIDList given");
        }

        if(excludeSet.contains("userDataList") && excludeSet.contains("ukDataList") ){
            return UserMasteryDataListDTO.builder().build();
        }


        List<UserKnowledge> knowledgeList = userKnowledgeRepo.getByUserIDList(userIDList);

        Set<Integer> ukSet = new HashSet<>();
        Map<Integer, List<Float>> ukIDMasteryListMap = new HashMap<>();
        Map<String, List<UserKnowledge>> userIDKnowledgeMap =new HashMap<>();
        knowledgeList.forEach(know -> {
            //Only if ukData List is needed
            if(!excludeSet.contains("ukDataList")){
                List<Float> list = null;
                if(!ukIDMasteryListMap.containsKey(know.getUkId())){
                    ukSet.add(know.getUkId());
                    list = new ArrayList<>();
                }
                else
                    list = ukIDMasteryListMap.get(know.getUkId());

                list.add(know.getUkMastery());

                ukIDMasteryListMap.put(know.getUkId(), list);
            }

            if(!excludeSet.contains("userDataList")){
                List<UserKnowledge> list = null;
                if(!userIDKnowledgeMap.containsKey(know.getUserUuid())){
                    ukSet.add(know.getUkId());
                    list = new ArrayList<>();
                }
                else
                    list = userIDKnowledgeMap.get(know.getUserUuid());

                list.add(know);

                userIDKnowledgeMap.put(know.getUserUuid(), list);
            }
        });    

        return UserMasteryDataListDTO.builder()
                                     .userDataList(!excludeSet.contains("userDataList") ? createUserDataList(userIDKnowledgeMap) : null)
                                     .ukDataList(!excludeSet.contains("ukDataList") ? createUkDataList(ukSet, ukIDMasteryListMap) : null)
                                     .build();
    }

    private List<UserUKKnowledgeDTO> createUserDataList(Map<String, List<UserKnowledge>> userIDKnowledgeMap){
        return userIDKnowledgeMap.entrySet().stream()
                                 .parallel()
                                 .map(entry -> {
                                     List<UkUserKnowledgeScoreDTO> scoreList
                                        = entry.getValue()
                                               .stream()
                                               .parallel()
                                                .map(uknow -> {
                                                    return UkUserKnowledgeScoreDTO.builder()
                                                                                    .ukID(uknow.getUkId())
                                                                                    .mastery((double)uknow.getUkMastery())
                                                                                    .build();
                                                })
                                                .collect(Collectors.toList());
                                                    
                                    return UserUKKnowledgeDTO.builder()
                                                             .userID(entry.getKey())
                                                             .ukKnowledgeList(scoreList)
                                                             .build();
                                 })
                                 .collect(Collectors.toList());

    }

    private List<UkDataDTO> createUkDataList(Set<Integer> ukSet, Map<Integer, List<Float>> ukIDMasteryListMap){
        //Get uk names
        List<Uk> everyUkList = (List<Uk>)ukRepo.findAllById(ukSet);
        Map<Integer, String> ukNameMap = everyUkList.stream().parallel().collect(Collectors.toMap(Uk::getUkId, Uk::getUkName));


        return ukNameMap.entrySet().stream().parallel().map(entry -> {
            return UkDataDTO.builder()
                            .name(entry.getValue())
                            .ukID(entry.getKey())
                            .stats(getStats(ukIDMasteryListMap.get(entry.getKey())))
                            .build();
        }).collect(Collectors.toList());
    }

    private GlobalStatisticDTO getStats(List<Float> masteryList) {
        Float mean = masteryList.stream().reduce(0.0f, Float::sum) / masteryList.size();


        return GlobalStatisticDTO.builder()
                                 .mean(mean)
                                 .build();
    }
}

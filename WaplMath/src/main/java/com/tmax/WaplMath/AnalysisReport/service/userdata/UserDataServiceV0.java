package com.tmax.WaplMath.AnalysisReport.service.userdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.BasicProblemStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.uk.UkDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserLRSRecordSimpleDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserMasteryDataListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserStudyDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserUKKnowledgeDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeScoreDTO;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.score.ScoreServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceV0;
import com.tmax.WaplMath.AnalysisReport.service.userknowledge.UserKnowledgeServiceBase;
// import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.Common.repository.uk.UkRepo;
import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

// import com.tmax.WaplMath.AnalysisReport.dto.statistics.BasicProblemStatDTO;
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
    public List<UserStudyDataDTO> getStudyStatList(List<String> userIDList, String from, String until, Set<String> excludeSet) {
        //TEMP: TODO: Map for median emulation. l -> 2min 30s, m -> 3min, h -> 3min 30s
        Map<String, Long> durMap = new HashMap<>();
        durMap.put("l", (long)(2*60 + 30)*1000); //low
        durMap.put("m", (long)(3*60 + 0)*1000); //mid
        durMap.put("h", (long)(3*60 + 30)*1000); //high
        durMap.put("u", (long)-1); //unknown

      
        List<UserStudyDataDTO> result = 
            userIDList.stream()
                      .parallel()
                      .flatMap(id -> {
                            BasicProblemStatDTO basic = null;
                            CustomStatDataDTO custom = null;                           

                            //Get the simple lrs history stat
                            Statistics userLrsHistory = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_LRS_STATEMENT_HISTORY);
                            if(userLrsHistory == null){
                                log.warn("No LRS history stat for {}", id);
                                return Stream.empty();
                            }

                            //Convert back to Object
                            List<UserLRSRecordSimpleDTO> historyList = Arrays.asList(new Gson().fromJson(userLrsHistory.getData(),UserLRSRecordSimpleDTO[].class));

                            //Filter the data by date range (if given)
                            if(from != null || until != null){
                                LocalDate fromTime = from != null ? LocalDate.parse(from) : null;
                                LocalDate untilTime = until != null ? LocalDate.parse(until) : null;

                                historyList = historyList.stream().filter(history -> {
                                    //True if time exists and is within range
                                    if(history.getTime() == null)
                                        return false;


                                    //Parse the times
                                    LocalDateTime time = LocalDateTime.parse(history.getTime());
                                    
                                    //From 00:00 of from
                                    if(from != null && time.compareTo(fromTime.atTime(0,0)) < 0){
                                        return false;
                                    }
                                    
                                    //Until 23:59 of until date
                                    if(until != null && time.compareTo(untilTime.atTime(23,59)) > 0) {
                                        return false;
                                    }

                                    return true;
                                }).collect(Collectors.toList());
                            }

                            //Basic counter
                            try {
                                int totalcnt = 0;
                                int correct = 0;
                                int wrong = 0;
                                int pass = 0;

                                // correct = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_CORRECT_CNT).getAsInt();
                                // pass = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_PASS_CNT).getAsInt();
                                // wrong = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_WRONG_CNT).getAsInt();
                                // totalcnt = userStatSvc.getUserStatistics(id, UserStatisticsServiceBase.STAT_RATE_PROBLEM_COUNT).getAsInt();
                                for(UserLRSRecordSimpleDTO record : historyList){
                                    switch(record.getCorr()){
                                        case "c":
                                            correct++;
                                            break;
                                        case "p":
                                            pass++;
                                            break;
                                        case "w":
                                            wrong++;
                                            break;
                                        default:
                                            continue;
                                    }

                                    totalcnt++;
                                }

                                basic = BasicProblemStatDTO.builder()
                                                            .correct(correct)
                                                            .wrong(wrong)
                                                            .pass(pass)
                                                            .totalcnt(totalcnt)
                                                            .build();
                            }
                            catch (Throwable e){
                                log.error("Count Stats not found. Check LRS or stat data of user {}. {}", id, e.getMessage());
                            }


                            //Custom fields
                            //INFO: Do not merge historyList loop for maintainance modularity

                            //Prepare tally fields
                            int pick = 0;
                            int unknown = 0;
                            int difficult = 0;
                            int miss = 0;

                            for(UserLRSRecordSimpleDTO record : historyList){
                                String diff = record.getDiff();
                                Long median = durMap.get(diff);
                                
                                //Pick: under 3000ms
                                if(record.getDur() < 3000)
                                    pick++;

                                //Unknown: case 1) w AND duration > median 2) PASS
                                if(record.getCorr().equals("p") && median > 0 && record.getDur() >= median)
                                        unknown++;

                                if(record.getCorr().equals("w")){
                                    //Difficult: w AND over total time 20 min
                                    if(record.getDur() > 20 * 60* 1000 )
                                        difficult++;

                                    //Miss : w AND duration under 3000ms or over median
                                    if(record.getDur() < 3000 || (median > 0 && record.getDur() >= median ) )
                                        miss++;
                                }
                            }

                            custom = CustomStatDataDTO.builder()
                                                        .pick(pick)
                                                        .difficult(difficult)
                                                        .unknown(unknown)
                                                        .miss(miss)
                                                        .notfocused(miss)
                                                        .notserious(difficult) //temp for compat
                                                        .build();


                            return Stream.of(UserStudyDataDTO.builder()
                                                .userID(id)
                                                .basic(basic)
                                                .custom(custom)
                                                .build());
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

    @Override
    public List<UserStudyDataDTO> getStudyStatList(List<String> userIDList, Set<String> excludeSet) {
        return getStudyStatList(userIDList, null, null, excludeSet);
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

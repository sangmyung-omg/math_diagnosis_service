package com.tmax.WaplMath.AnalysisReport.service.statistics.user;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserLRSRecordSimpleDTO;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUser;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUserKey;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.statistics.StatisticUserRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserExamScopeInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics.Type;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreData;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreServiceV0;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.problem.Problem;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.repository.problem.ProblemRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import com.tmax.WaplMath.Common.util.lrs.ActionType;
import com.tmax.WaplMath.Common.util.lrs.LRSManager;
// import com.tmax.WaplMath.Recommend.util.LRSAPIManager;
import com.tmax.WaplMath.Common.util.lrs.SourceType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("UserStatisticsServiceV0")
public class UserStatisticsServiceV0 implements UserStatisticsServiceBase {
    @Autowired
    private UserRepo userRepository;


    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    private UserKnowledgeRepo userKnowledgeRepo;

    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Autowired
    private StatisticUserRepo statisticUserRepo;

    @Autowired
    @Qualifier("CurrStatisticsServiceV0")
    CurrStatisticsServiceBase currStatSvc;

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    private CurriculumInfoRepo curriculumInfoRepo;

    @Autowired
    @Qualifier("AR-UserExamScopeInfoRepo")
    private UserExamScopeInfoRepo examScopeRepo;

    @Autowired
    @Qualifier("AR-ExamScopeUtil")
    private ExamScopeUtil examScopeUtil;

    @Autowired
    private LRSManager lrsManager;

    @Autowired
    private ProblemRepo probRepo;

    @Autowired
    private WaplScoreServiceV0 waplScoreSvc;

    /*
    *      ┌─────────────┐
    *      │   <START>   │
    *      └──────┬──────┘
    *             │
    *      ┌──────▼───────┐
    *      │(For given ID)│
    *      └──────┬───────┘
    *             │
    *      ┌──────▼────────┐
    *      │(Get all mas-  │
    *      │teries of user)│
    *      └────────────┬──┘
    *                   │
    *      ┌────────────▼─────────────┐
    *      │   (Generate Statistics)  │
    *      └────────────┬─────────────┘
    *                   │
    *      ┌────────────▼─────────────┐
    *      │(Save to UK Statistics DB)│
    *      └──────────────────────────┘
    */
    @Override
    public Set<StatsAnalyticsUser> updateSpecificUser(String userID, boolean updateDB) {
        //Check if User is vali
        Optional<User> user = userRepository.findById(userID);
        if(!user.isPresent()){
            log.error("User {} is not valid. skipping stat.", userID);
            return null;
        }

        //Prepare the update timestamp
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        //Create set for DB update
        Set<StatsAnalyticsUser> updateSet = IntStream.range(0, 4)
                                                    .parallel()
                                                    .mapToObj(idx -> parallelRunner(userID, now, idx))
                                                    .flatMap(set -> set.stream())
                                                    .collect(Collectors.toSet());

        //Save to DB
        if(updateDB){
            log.info("Saving for user: {}, # in set = {}", userID, updateSet.size());
            statisticUserRepo.saveAll(updateSet);
            log.info("Saved. " + userID);
            return null;
        }

        return updateSet;
    }

    @Override
    public void updateSpecificUser(String userID) {
        updateSpecificUser(userID, true);
    }

    //Indexed stat selector for parallel processing
    private Set<StatsAnalyticsUser> parallelRunner(String userID, Timestamp ts, Integer funcIdx) {
        Set<StatsAnalyticsUser> output = null;
        
        switch(funcIdx) {
            case 0:
                output = getUKGlobalStats(userID, ts);
                break;
            case 1:
                output = getPerCurriculumStats(userID, ts);
                break;
            case 2:
                output = getExamScopeStats(userID, ts);
                break;
            case 3:
                output = getLRSStatistics(userID, ts);
                break;
            default:
                output = new HashSet<>();
        }

        return output;
    }

    private Set<StatsAnalyticsUser> getWaplScoreStats(String userID, Float examScopeScore, Timestamp ts){
        //Check if wapl score and mastery exists
        if(hasUserStatistics(userID, STAT_WAPL_SCORE) && hasUserStatistics(userID, STAT_WAPL_SCORE_MASTERY)){
            log.debug("Using exising wapl stats. [{}]", userID);
            return new HashSet<>();
        }

        log.debug("Creating new waplscore stats for user: " + userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> updateSet = new HashSet<>();

        WaplScoreData data = waplScoreSvc.generateWaplScore(userID, examScopeScore, false);

        //Create stat statements and add to set
        //waplscore
        updateSet.add(StatsAnalyticsUser.builder()
                                        .userId(userID)
                                        .name(STAT_WAPL_SCORE)
                                        .type(Statistics.Type.FLOAT.getValue())
                                        .data(data.getScore().toString())
                                        .lastUpdate(ts)
                                        .build()
                                         );
        //waplscore mastery data
        updateSet.add(StatsAnalyticsUser.builder()
                                        .userId(userID)
                                        .name(STAT_WAPL_SCORE_MASTERY)
                                        .type(Statistics.Type.FLOAT.getValue())
                                        .data(data.getMasteryJson())
                                        .lastUpdate(ts)
                                        .build()
                                        );
        return updateSet;
    }

    private Set<StatsAnalyticsUser> getUKGlobalStats(String userID, Timestamp ts){
        log.debug("Creating global uk stats for user: " + userID);

        //Get user's knowledge list.
        List<UserKnowledge> knowledgeList = userKnowledgeRepo.getByUserUuid(userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> updateSet = new HashSet<>();

        //If knowledga data is invalid.
        if(knowledgeList == null || knowledgeList.size() == 0){
            log.warn("User statistics not updated:" + userID);
            return updateSet;
        }


        //Parallel stream
        List<Float> masteryList = knowledgeList.stream().parallel().map(UserKnowledge::getUkMastery).collect(Collectors.toList());


        //Calc each UK statistics and push to set
        //mean of mastery --> current score
        updateSet.add(statsToAnalyticsUser(userID, 
                                            new Statistics(STAT_TOTAL_MASTERY_MEAN, Statistics.Type.FLOAT, ukStatSvc.getMean(masteryList).toString()), 
                                            ts));

        updateSet.add(statsToAnalyticsUser(userID, 
                                            new Statistics(STAT_TOTAL_MASTERY_STD, Statistics.Type.FLOAT, ukStatSvc.getSTD(masteryList).toString()), 
                                            ts));

                                            

        return updateSet;
    }

    private Set<StatsAnalyticsUser> getPerCurriculumStats(String userID, Timestamp ts) {
        log.debug("Creating curriculum stats for user: " + userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> output = new HashSet<>();

        Map<String, Float> currMasteryMap = currStatSvc.getCurriculumMasteryOfUser(userID);

        //Convert map to json string
        String mapJson = new Gson().toJson(currMasteryMap);

        //Build update set.
        output.add(statsToAnalyticsUser(userID,
                                        Statistics.builder()
                                                .name(STAT_CURRICULUM_MASTERY_MAP)
                                                .type(Statistics.Type.JSON)
                                                .data(mapJson)
                                                .build(),
                                        ts) );

        return output;
    }

    private Set<StatsAnalyticsUser> getExamScopeStats(String userID, Timestamp ts){
        log.debug("Creating examscope stats stats for user: " + userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> output = new HashSet<>();

        //Get user curriculumList from examscope
        List<String> currIDList = examScopeUtil.getCurrIdListOfScope(userID);


        //Get userknowledge list with currList scope
        List<UserKnowledge> knowledgeList = userKnowledgeRepo.getByUserUuidScoped(userID, currIDList);

        // List<Float> masteryList = new ArrayList<>();
        // knowledgeList.forEach(uknow -> masteryList.add(uknow.getUkMastery()));
        List<Float> masteryList = knowledgeList.stream()
                                                .parallel()
                                                .map(UserKnowledge::getUkMastery)
                                                .collect(Collectors.toList());

        Float examScopeScore = ukStatSvc.getMean(masteryList);

        if(examScopeScore.isNaN()){
            log.error("Cannot create examscope score for {}. Score is NaN", userID);
            return output;
        }

        output.add(statsToAnalyticsUser(userID, 
                                            new Statistics(STAT_EXAMSCOPE_SCORE, Statistics.Type.FLOAT, examScopeScore.toString()), 
                                            ts));


        //Update score history (DEBUG use)
        //Get if exist
        List<Float> historyList = null;
        Statistics historyStat = getUserStatistics(userID, STAT_EXAMSCOPE_SCORE_HISTORY);
        if(historyStat != null){
            try { historyList = historyStat.getAsFloatList();}
            catch(Throwable e){log.error("Exisiting examscope history for {} is invalid. generating new list.", userID);}
        }

        if(historyList == null)
            historyList = new ArrayList<>();
        
        //Append score if last result is not same as before or empty
        final long PRECISION_COMPARE_LEVEL = (long)Math.pow(10, 7);
        if( historyList.size() == 0 ||  
           (
                historyList.size() > 0 && 
                (
                   (long)(historyList.get(historyList.size() - 1).floatValue() * PRECISION_COMPARE_LEVEL) != (long)(examScopeScore.floatValue())*PRECISION_COMPARE_LEVEL ) 
                ) 
           ){
            historyList.add(examScopeScore);
            output.add(statsToAnalyticsUser(userID, 
                                            Statistics.builder().name(STAT_EXAMSCOPE_SCORE_HISTORY).type(Type.FLOAT_LIST).data(historyList.toString()).build(),
                                            ts));
        }


        //Run waplscore get here (examscore dependent)
        output.addAll(getWaplScoreStats(userID, examScopeScore, ts));

        return output;
    }

    @Override
    public void updateAllUsers() {
        log.info("Updating Statistics of all users");

        //Get all user list
        List<User> userList = (List<User>)userRepository.findAll();

        Set<StatsAnalyticsUser> updateSet = 
                    userList.stream()
                            .parallel()
                            .flatMap(user -> {
                                log.info(String.format("Updating statistics for user [%s] (%s)",user.getUserUuid(), user.getName()));

                                //Get user update set
                                Set<StatsAnalyticsUser> userUpdateSet = updateSpecificUser(user.getUserUuid(), false);

                                if(userUpdateSet == null){
                                    log.error("Cannot create update set for user [{}] ({})", user.getUserUuid(), user.getName());
                                    return Stream.empty();
                                }

                                return userUpdateSet.stream();
                            })
                            .collect(Collectors.toSet());

        //Update to DB
        log.info("Saving user stats for {} users.", updateSet.size());
        statisticUserRepo.saveAll(updateSet);
        log.info("Saved successfully.");
    }

    @Override
    public int updateCustomUserStat(String userID, String statName, Statistics.Type dataType, String data){
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        statisticUserRepo.save(statsToAnalyticsUser(
                                                    userID, 
                                                    new Statistics(statName, dataType, data), 
                                                    now));
        return 0;
    }
    


    private StatsAnalyticsUser statsToAnalyticsUser(String userID, Statistics stats, Timestamp now){
        return statsToAnalyticsUser(userID, "", stats, now);
    }

    private StatsAnalyticsUser statsToAnalyticsUser(String userID, String prefix, Statistics stats, Timestamp ts){
        return StatsAnalyticsUser.builder()
                                .userId(userID)
                                .name(prefix + stats.getName())
                                .type(stats.getType().getValue())
                                .data(stats.getData())
                                .lastUpdate(ts)
                                .build();
    }

    @Override
    public Statistics getUserStatistics(String userID, String statName) {
        StatsAnalyticsUserKey searchKey = new StatsAnalyticsUserKey(userID, statName);
        
        Optional<StatsAnalyticsUser> result = statisticUserRepo.findById(searchKey);

        //If no result
        if(!result.isPresent())
            return null;

        StatsAnalyticsUser stat = result.get();
        Statistics output = Statistics.builder()
                                      .name(statName)   
                                      .type(Statistics.Type.getFromValue(stat.getType()))
                                      .data(stat.getData())
                                      .build();
        return output;    
    }

    @Override
    public boolean hasUserStatistics(String userID, String statName) {
        StatsAnalyticsUserKey searchKey = new StatsAnalyticsUserKey(userID, statName);
        Optional<StatsAnalyticsUser> result = statisticUserRepo.findById(searchKey);

        return result.isPresent();
    }

    
    /**
     * 
     */
    private Set<StatsAnalyticsUser> getLRSStatistics(String userID, Timestamp ts) {
        log.debug("Creating lrs stats for user: " + userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> updateSet = new HashSet<>();


        //Get LRS statement list for user
        List<LRSStatementResultDTO> statementList = lrsManager.getStatementList(userID, ActionType.getAllActionTypes(), SourceType.getAllSourceTypes());

        //If size == 0  retry with hyphened version TEMP. FIXME. only try if uuid is a 32 sized one;
        // if(userID.length() == 32 && statementList.size() == 0){
        //     String formatedUserID = String.format("%s-%s-%s-%s-%s", userID.substring(0,8),
        //                                                             userID.substring(8, 12),
        //                                                             userID.substring(12,16),
        //                                                             userID.substring(16,20),
        //                                                             userID.substring(20, 32));
        //     statementList = lrsManager.getStatementList(formatedUserID, actionTypeList, sourceTypeList);                                                  
        // }
        

        //if still null --> then return. do not create a update set
        if(statementList.size()==0){
            log.warn("No valid statement found. Unable to create LRS stats");
            return updateSet;
        }

        // build set for problem info query
        Set<Integer> probIDSet = statementList.stream()
                                              .parallel()
                                              .flatMap(s -> {
                                                  try {
                                                    return Stream.of( Integer.valueOf( s.getSourceId() ) );
                                                  }
                                                  catch (Exception e){
                                                      log.warn("Source ID invalid. user {}. {}", userID, s.toString());
                                                      return Stream.empty();
                                                  }
                                              })
                                              .collect(Collectors.toSet());

        //Get the probList and create map to get difficulty
        Map<Integer, String> probDiffMap = 
            ((List<Problem>)probRepo.findAllById(probIDSet))
                                           .stream()
                                           .parallel()
                                           .collect(Collectors.toMap(Problem::getProbId, Problem::getDifficulty));
        

        //Tallys for correct rate and duration count
        Integer correctTally = 0;
        Integer passTally = 0;
        // Integer wrongTally = 0; //wrong = total - correct - pass
        Integer speedSatisfyTally = 0;

        Integer totalTally = 0;

        Set<String> recentCurrSet = new LinkedHashSet<>();
        Set<String> diagRecentCurrSet = new LinkedHashSet<>();
        // List<String> recentCurrList = new ArrayList<>();

        for(LRSStatementResultDTO statement: statementList){
            //Get source ID and validate it
            String srcID = statement.getSourceId();

            //Skip if invalid
            if(srcID == null){log.warn("Invalid src ID. {} {}", userID, statement.toString()); continue;}

            //Get probID with exception
            Integer probID = null;
            try { probID = Integer.valueOf(srcID);}
            catch(Exception e) {
                log.warn("Skipping un-parsable problem id. May-not be an Integer format. [{}]. {}.", userID, srcID);
                continue;
            }

            //Get the raw duration string(as it canbe null)
            String durationRaw = statement.getDuration();

            //If null --> consider as fail
            if(durationRaw == null){
                continue; 
            }

            
            Integer duration = Integer.valueOf(durationRaw);
            String difficulty = probDiffMap.get(probID);

            //Check if diff exist --> is it a valid problem
            if(difficulty == null){
                log.warn("Diff is null. problem [{}]. Skipping stat entry for this prob. user {}", probID, userID);
                continue;
            }

            //Build stats from here (No more skip conditions)
            //Get curr ID of problem -> add to recent set
            String currID = curriculumInfoRepo.getCurrIdByProbId(probID);
            if(currID != null && !currID.isEmpty()){
                //Add to recent
                recentCurrSet.add(currID);

                //If diag. add to diag recent too
                if(Arrays.asList("diagnosis", "diagnosis_simple").contains( statement.getSourceType() ) )
                    diagRecentCurrSet.add(currID);
            }

            //Get correct histogram
            if(statement.getIsCorrect() != null && statement.getIsCorrect() > 0){
                correctTally++;
            }

            if(statement.getUserAnswer() != null && statement.getUserAnswer().equals("PASS")){
                passTally++;
            }

            if(difficulty.equals("상") && duration < (3 * 60 + 30 )* 1000){
                speedSatisfyTally++;
            }
            else if(difficulty.equals("중") && duration < (3 * 60 + 0 )* 1000){
                speedSatisfyTally++;
            }
            else if(difficulty.equals("하") && duration < (2 * 60 + 30 )* 1000){
                speedSatisfyTally++;
            }

            totalTally++;
        }


        //Make the statistics
        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_CORRECT_RATE)
                                                      .type(Statistics.Type.FLOAT)
                                                      .data(Float.toString((float)correctTally / totalTally))
                                                      .build(), 
                                            ts));

        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_SOLVING_SPEED_SATISFY_RATE)
                                                      .type(Statistics.Type.FLOAT)
                                                      .data(Float.toString((float)speedSatisfyTally / totalTally))
                                                      .build(), 
                                            ts));

        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_RATE_PROBLEM_COUNT)
                                                      .type(Statistics.Type.INT)
                                                      .data(Integer.toString(totalTally))
                                                      .build(), 
                                            ts));
        
        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_CORRECT_CNT)
                                                      .type(Statistics.Type.INT)
                                                      .data(Integer.toString(correctTally))
                                                      .build(), 
                                            ts));

        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_PASS_CNT)
                                                      .type(Statistics.Type.INT)
                                                      .data(Integer.toString(passTally))
                                                      .build(), 
                                            ts));

        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_WRONG_CNT)
                                                      .type(Statistics.Type.INT)
                                                      .data(Integer.toString(totalTally - correctTally - passTally))
                                                      .build(), 
                                            ts));
        
        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_RECENT_CURR_ID_LIST)
                                                      .type(Statistics.Type.STRING_LIST)
                                                      .data(recentCurrSet.toString())
                                                      .build(), 
                                            ts));

        //Save the lrs statement list to user history DB (near RAW DATA)
        List<UserLRSRecordSimpleDTO> recordList = 
                statementList.stream().parallel()
                             .flatMap(statement -> {
                                 try{
                                    //Get basic info
                                    Integer probID = Integer.parseInt(statement.getSourceId());
                                    // String userId = statement.getUserId();

                                    //Get duration info
                                    String durationRaw = statement.getDuration();
                                    Long duration = Long.parseLong(durationRaw == null ? "0" : (String)durationRaw);

                                    //Get
                                    Integer isCorrect = statement.getIsCorrect();
                                    String userAnswer = statement.getUserAnswer();

                                    String correct = ""; // c / p / w
                                    if(userAnswer.toUpperCase().equals("PASS")){
                                        correct = "p";
                                    }
                                    else {
                                        correct = isCorrect == null || isCorrect == 0 ? "w": "c";
                                    }

                                    //Difficulty
                                    String difficulty = null;
                                    String diffRaw = probDiffMap.get(probID);
                                    if(diffRaw.equals("상"))
                                        difficulty = "l";
                                    else if(diffRaw.equals("중"))
                                        difficulty = "m";
                                    else if(diffRaw.equals("하"))
                                        difficulty = "h";
                                    else
                                        difficulty = "u"; //unknown

                                    return Stream.of(UserLRSRecordSimpleDTO.builder().pID(probID)
                                                                                    // .userID(userId)
                                                                                    .diff(difficulty)
                                                                                    .dur(duration).corr(correct).build());
                                 }
                                 catch(Exception e){
                                     log.error("LRS error at {}. {}.", statement.toString(), e.getMessage());
                                     return Stream.empty();
                                 }
                             })
                             .collect(Collectors.toList());

        String userLRSRecordJson = new Gson().toJson(recordList);
        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                    .name(STAT_LRS_STATEMENT_HISTORY)
                                                    .type(Statistics.Type.STRING)
                                                    .data(userLRSRecordJson)
                                                    .build(), 
                                            ts));           

        return updateSet;
    }
    
}

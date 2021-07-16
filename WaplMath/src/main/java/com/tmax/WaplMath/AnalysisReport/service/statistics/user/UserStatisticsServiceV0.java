package com.tmax.WaplMath.AnalysisReport.service.statistics.user;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUser;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUserKey;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.statistics.StatisticUserRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserExamScopeInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.Recommend.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.UserRepository;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("UserStatisticsServiceV0")
public class UserStatisticsServiceV0 implements UserStatisticsServiceBase {
    @Autowired
    private UserRepository userRepository;


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
    private LRSAPIManager lrsApiManager;

    @Autowired
    private ProblemRepo probRepo;


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
    public void updateSpecificUser(String userID) {
        //Create set for DB update
        Set<StatsAnalyticsUser> updateSet = new HashSet<>();

        //Prepare the update timestamp
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        //Add global stats
        updateSet.addAll(getUKGlobalStats(userID, now));

        //Add curriculum stats builder
        updateSet.addAll(getPerCurriculumStats(userID, now));

        //Add examscore stats
        updateSet.addAll(getExamScopeStats(userID, now));

        //Add LRS stats
        updateSet.addAll(getLRSStatistics(userID, now));

        //Save to DB
        log.info("Saving for user:" + userID);
        statisticUserRepo.saveAll(updateSet);
        log.info("Saved. " + userID);
    }

    private Set<StatsAnalyticsUser> getUKGlobalStats(String userID, Timestamp ts){
        log.info("Creating global uk stats for user: " + userID);

        //Get user's knowledge list.
        List<UserKnowledge> knowledgeList = userKnowledgeRepo.getByUserUuid(userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> updateSet = new HashSet<>();

        //If knowledga data is invalid.
        if(knowledgeList == null || knowledgeList.size() == 0){
            log.warn("User statistics not updated:" + userID);
            return updateSet;
        }

        //Extract uk mastery from data and create new list
        List<Float> masteryList = new ArrayList<>();
        knowledgeList.forEach(uknow -> masteryList.add(uknow.getUkMastery()));


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
        log.info("Creating curriculum stats for user: " + userID);

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
        log.info("Creating examscope stats stats for user: " + userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> output = new HashSet<>();

        //Get user curriculumList from examscope
        List<String> currIDList = examScopeUtil.getCurrIdListOfScope(userID);


        //Get userknowledge list with currList scope
        List<UserKnowledge> knowledgeList = userKnowledgeRepo.getByUserUuidScoped(userID, currIDList);

        List<Float> masteryList = new ArrayList<>();
        knowledgeList.forEach(uknow -> masteryList.add(uknow.getUkMastery()));

        output.add(statsToAnalyticsUser(userID, 
                                            new Statistics(STAT_EXAMSCOPE_SCORE, Statistics.Type.FLOAT, ukStatSvc.getMean(masteryList).toString()), 
                                            ts));

        return output;
    }

    @Override
    public void updateAllUsers() {
        log.info("Updating Statistics of all users");

        //Get all user list
        Iterable<User> userList = userRepository.findAll();

        for(User user : userList){
            log.info(String.format("Updating user [%s] (%s)",user.getUserUuid(), user.getName()));
            updateSpecificUser(user.getUserUuid());
        }
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
        log.info("Creating lrs stats for user: " + userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> updateSet = new HashSet<>();


        //Get LRS statement list for user
        List<LRSStatementResultDTO> statementList = lrsApiManager.getUserStatement(userID);

        //If size == 0  retry with hyphened version TEMP. FIXME. only try if uuid is a 32 sized one;
        if(userID.length() == 32 && statementList.size() == 0){
            String formatedUserID = String.format("%s-%s-%s-%s-%s", userID.substring(0,8),
                                                                    userID.substring(8, 12),
                                                                    userID.substring(12,16),
                                                                    userID.substring(16,20),
                                                                    userID.substring(20, 32));
            statementList = lrsApiManager.getUserStatement(formatedUserID);                                                   
        }

        //if still null --> then return. do not create a update set
        if(statementList.size()==0){
            log.warn("No valid statement found. Unable to create LRS stats");
            return updateSet;
        }

        // build set for problem info query
        Set<Integer> probIDSet = statementList.stream().map(s -> Integer.valueOf(s.getSourceId()) ).collect(Collectors.toSet());

        //Get the probList and create map to get difficulty
        Map<Integer, String> probDiffMap = new HashMap<>();
        probRepo.findAllById(probIDSet).forEach(p -> probDiffMap.put(p.getProbId(), p.getDifficulty()));
        

        //Tallys for correct rate and duration count
        Integer correctTally = 0;
        Integer speedSatisfyTally = 0;

        Set<String> recentCurrSet = new LinkedHashSet<>();
        // List<String> recentCurrList = new ArrayList<>();

        for(LRSStatementResultDTO statement: statementList){
            //Get probID duration
            Integer probID = Integer.valueOf(statement.getSourceId());

            //Get curr ID of problem
            recentCurrSet.add(curriculumInfoRepo.getProblemByCurriculumID(probID));

            //Get correct histogram
            if(statement.getIsCorrect() != null && statement.getIsCorrect() > 0){
                correctTally++;
            }

            //Get the raw duration string(as it canbe null)
            String durationRaw = statement.getDuration();

            //If null --> consider as fail
            if(durationRaw == null){
                continue; 
            }

            
            Integer duration = Integer.valueOf(durationRaw);
            String difficulty = probDiffMap.get(probID);

            if(difficulty.equals("상") && duration < (3 * 60 + 30 )* 1000){
                speedSatisfyTally++;
            }
            else if(difficulty.equals("중") && duration < (3 * 60 + 0 )* 1000){
                speedSatisfyTally++;
            }
            else if(difficulty.equals("하") && duration < (2 * 60 + 30 )* 1000){
                speedSatisfyTally++;
            }
        }


        //Make the statistics
        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_CORRECT_RATE)
                                                      .type(Statistics.Type.FLOAT)
                                                      .data(Float.toString((float)correctTally / statementList.size()))
                                                      .build(), 
                                            ts));

        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_SOLVING_SPEED_SATISFY_RATE)
                                                      .type(Statistics.Type.FLOAT)
                                                      .data(Float.toString((float)speedSatisfyTally / statementList.size()))
                                                      .build(), 
                                            ts));

        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_RATE_PROBLEM_COUNT)
                                                      .type(Statistics.Type.INT)
                                                      .data(Integer.toString(statementList.size()))
                                                      .build(), 
                                            ts));
        
        updateSet.add(statsToAnalyticsUser( userID, 
                                            Statistics.builder()
                                                      .name(STAT_RECENT_CURR_ID_LIST)
                                                      .type(Statistics.Type.STRING_LIST)
                                                      .data(recentCurrSet.toString())
                                                      .build(), 
                                            ts));
        return updateSet;
    }
    
}

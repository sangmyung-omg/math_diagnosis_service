package com.tmax.WaplMath.AnalysisReport.service.statistics.user;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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


    //logger
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());


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

        //Save to DB
        logger.info("Saving for user:" + userID);
        statisticUserRepo.saveAll(updateSet);
        logger.info("Saved. " + userID);
    }

    private Set<StatsAnalyticsUser> getUKGlobalStats(String userID, Timestamp ts){
        logger.info("Creating global uk stats for user: " + userID);

        //Get user's knowledge list.
        List<UserKnowledge> knowledgeList = userKnowledgeRepo.getByUserUuid(userID);

        //Create set for DB update
        Set<StatsAnalyticsUser> updateSet = new HashSet<>();

        //If knowledga data is invalid.
        if(knowledgeList == null || knowledgeList.size() == 0){
            logger.warn("User statistics not updated:" + userID);
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
        logger.info("Creating curriculum stats for user: " + userID);

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
        logger.info("Creating examscope stats stats for user: " + userID);

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
        //Get all user list
        Iterable<User> userList = userRepository.findAll();

        for(User user : userList){
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

    private StatsAnalyticsUser statsToAnalyticsUser(String userID, String prefix, Statistics stats, Timestamp now){
        return new StatsAnalyticsUser(userID, prefix + stats.getName(), stats.getType().getValue(),stats.getData(), now);
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
}

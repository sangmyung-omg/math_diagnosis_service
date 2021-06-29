package com.tmax.WaplMath.AnalysisReport.service.statistics;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUser;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.statistics.StatisticUserRepo;
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
        //Get user's knowledge list.
        List<UserKnowledge> knowledgeList = userKnowledgeRepo.getByUserUuid(userID);

        //If the knowledge data is valid
        if(knowledgeList != null && knowledgeList.size() > 0){
            //Extract uk mastery from data and create new list
            List<Float> masteryList = new ArrayList<>();
            knowledgeList.forEach(uknow -> masteryList.add(uknow.getUkMastery()));

            //Create set for DB update
            Set<StatsAnalyticsUser> updateSet = new HashSet<>();

            //Prepare the update timestamp
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            //Calc each UK statistics and push to set

            //mean of mastery --> current score
            updateSet.add(statsToAnalyticsUser(userID, 
                                               new Statistics("mastery_mean", Statistics.Type.FLOAT, ukStatSvc.getMean(masteryList).toString()), 
                                               now));

            updateSet.add(statsToAnalyticsUser(userID, 
                                               new Statistics("mastery_std", Statistics.Type.FLOAT, ukStatSvc.getSTD(masteryList).toString()), 
                                               now));



            //Save to DB
            logger.info("Saving for user:" + userID);
            statisticUserRepo.saveAll(updateSet);
            logger.info("Saved. " + userID);
        }
        else {
            
        }
    }

    @Override
    public void updateAllUsers() {
        //Get all user list
        Iterable<User> userList = userRepository.findAll();

        for(User user : userList){
            updateSpecificUser(user.getUserUuid());
        }
    }

    private StatsAnalyticsUser statsToAnalyticsUser(String userID, Statistics stats, Timestamp now){
        return statsToAnalyticsUser(userID, "", stats, now);
    }

    private StatsAnalyticsUser statsToAnalyticsUser(String userID, String prefix, Statistics stats, Timestamp now){
        return new StatsAnalyticsUser(userID, prefix + stats.getName(), stats.getType().getValue(),stats.getData(), now);
    }
}

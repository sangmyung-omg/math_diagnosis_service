package com.tmax.WaplMath.AnalysisReport.service.statistics;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUk;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.statistics.StatisticUKRepo;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.uk.Uk;
import com.tmax.WaplMath.Recommend.repository.UkRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service("UKStatisticsServiceV0")
public class UKStatisticsServiceV0 implements UKStatisticsServiceBase{
    
    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    UserKnowledgeRepo userKnowledgeRepo;

    @Autowired
    UkRepository ukRepository;


    @Autowired
    StatisticUKRepo statisticUKRepo;

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    /**
     * Method to update/save all uk key based statistics
    * ┌─────────────┐
    * │   <START>   │
    * └──────┬──────┘
    *        │
    * ┌──────▼──────┐
    * │(Get All UKs)│
    * └──────┬──────┘
    *        │
    * ┌──────▼──────┐   ┌─────────────────────────┐
    * │ Foreach  UK ├───►(Get All UK related data)│
    * └─────────────┘   └────────────┬────────────┘
    *                                │
    *                   ┌────────────▼────────────┐ ┌──────────────────────────┐
    *                   │      Foreach Data       ├─►   (Generate Statistics)  │
    *                   └─────────────────────────┘ └────────────┬─────────────┘
    *                                                            │
    *                                               ┌────────────▼─────────────┐
    *                                               │(Save to UK Statistics DB)│
    *                                               └──────────────────────────┘
     */
    @Override
    public int updateAllStatistics() {

        //Get all UK in DB.
        List<Uk> ukList = ukRepository.findAll();

        //Get all user in DB

        //Get current timestamp(for uniform update time)
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        //Prepare update set
        Set<StatsAnalyticsUk> updateSet = new HashSet<>();

        //For each uk. --> UK based loop
        for(Uk uk: ukList) {
            //Get the knowledge list for each uk
            List<UserKnowledge> userKnowledgeList = userKnowledgeRepo.getByUkId(uk.getUkId());
            
            if(userKnowledgeList != null && userKnowledgeList.size() > 0){
                //List to save mastery data
                List<Float> masteryList = new ArrayList<>();
                userKnowledgeList.forEach(uknow->masteryList.add(uknow.getUkMastery()));


                //Create various statistics data (Clob string) and add to update Set     
                //mean
                updateSet.add(statToAnalyticsUk(uk.getUkId(), 
                                                new Statistics("mastery_mean", Statistics.Type.FLOAT, getMean(masteryList).toString() ), 
                                                now));

                //sorted list (for percentile calc)
                updateSet.add(statToAnalyticsUk(uk.getUkId(), 
                                                new Statistics("mastery_sorted", Statistics.Type.FLOAT_LIST, getSorted(masteryList).toString() ), 
                                                now));

                //standard deviation
                updateSet.add(statToAnalyticsUk(uk.getUkId(), 
                                                new Statistics("mastery_std", Statistics.Type.FLOAT, getSTD(masteryList).toString() ), 
                                                now));
            }
        }

        //Update the stat set to DB
        logger.info("Saving sets: " + updateSet.size());
        statisticUKRepo.saveAll(updateSet);
        logger.info("Saved set");
        

        return 0;
    }

    /**
     * Method to convert internal Statistics struct to uk
     * @param input
     * @return
     */
    private StatsAnalyticsUk statToAnalyticsUk(Integer integer, Statistics input, Timestamp timestamp){
        return new StatsAnalyticsUk(integer, input.getName(), input.getType().getValue(), input.getData(), timestamp);
    }

    /**
     * returns mean value of mastery list
     * @param masteryList list of uk mastery value
     * @return
     */
    @Override
    public Float getMean(List<Float> masteryList){
        int count = 0;
        float total = 0.0f;
        for(Float mas: masteryList){total += mas; count++;}

        return total/count;
    }

    /**
     * Returns sorted mastery list
     * @param masteryList list of uk mastery value
     * @return
     */
    @Override
    public List<Float> getSorted(List<Float> masteryList){
        //Copy to new list
        List<Float> sortedList = new ArrayList<>(masteryList);

        //Sort list
        Collections.sort(sortedList);

        return sortedList;
    }

    /**
     * Returns standard deviation of mastery list
     * @param masteryList
     * @return
     */
    @Override
    public Float getSTD(List<Float> masteryList){
        //Calculate mean
        int count = 0;
        float mean = 0.0f;
        for(Float mas: masteryList){mean += mas; count++;}
        mean /= (float)count;

        //Calc STD
        float std = 0.0f;
        for(Float mas: masteryList){std += Math.pow(mas -  mean, 2);}
        std = (float)Math.sqrt(std/count);

        return std;
    }

    /**
     * Returns percentile position of given score
     * @param score
     * @param sortedMastery
     * @return
     */
    @Override
    public Float getPercentile(Float score, List<Float> sortedMastery){
        int index = 0;
        for(Float current: sortedMastery){
            //If the mastery in list has overcome the given score --> then that is the score's position
            if(score < current)
                break;

            index++;
        }

        return (float)index / sortedMastery.size();
    }
}

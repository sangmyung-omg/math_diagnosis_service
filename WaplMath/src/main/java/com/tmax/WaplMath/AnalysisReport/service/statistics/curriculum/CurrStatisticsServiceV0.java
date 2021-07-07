package com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsCurr;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsCurrKey;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.statistics.StatisticCurrRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.statistics.IScreamEduDataReader;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


class MasteryStat {
    private Float score = 0.0f;
    private int count = 0;

    public MasteryStat(){}

    public void addScore(Float score) {
        count++;

        //If nan. add zero
        if(score.isNaN()){
            return;
        }

        this.score += score; 
    }
    public float getAverage(){
        if(count == 0)
            return 0.0f;

        return this.score/count;        
    }
    public float getScore(){return this.score;}
}

@Service("CurrStatisticsServiceV0")
public class CurrStatisticsServiceV0 implements CurrStatisticsServiceBase {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private StatisticCurrRepo statisticCurrRepo;

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    private CurriculumInfoRepo curriculumInfoRepo;

    @Autowired
    private UserKnowledgeRepo userKnowledgeRepo;

    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Autowired
    private IScreamEduDataReader iScreamEduDataReader;

    @Override
    public void updateStatistics() {
        //Get all curriculums
        Iterable<Curriculum> currList = curriculumInfoRepo.findAll();

        //Create hashset
        Set<StatsAnalyticsCurr> updateSet = new HashSet<>();

        //Prepare the updateSet for current curriculum and map it from the mastery map
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        //For all curriculum
        for(Curriculum curr: currList){
            //Get related UK lists.
            List<UserKnowledge> uknowList = userKnowledgeRepo.getAllByLikelyCurrID(curr.getCurriculumId());

            //Add i-scream edu data
            if(iScreamEduDataReader.useIScreamData())
                uknowList.addAll(iScreamEduDataReader.getByLikelyCurriculumID(curr.getCurriculumId()));


            //If no uknow is selected, then continue. Further steps are useless
            if(uknowList == null || uknowList.size() == 0)
                continue;

            //Create map <userid, masterystat>
            Map<String, MasteryStat> masteryMap = new HashMap<>();
            for(UserKnowledge uknow: uknowList){
                String userID = uknow.getUserUuid();

                //If there is no key (not initialized)
                if(!masteryMap.containsKey(userID)){
                    masteryMap.put(userID, new MasteryStat());
                }

                //Add all mastery to stat per user
                masteryMap.get(userID).addScore(uknow.getUkMastery());
            }

            //Create a List with mastery average then sort it --> create sorted list
            List<Float> sortedMasteryList = new ArrayList<>();
            for(Map.Entry<String, MasteryStat> entry: masteryMap.entrySet()){
                sortedMasteryList.add((float)entry.getValue().getAverage());
            }
            Collections.sort(sortedMasteryList); //sort



            //Add statistics to update set
            updateSet.add(statToAnalyticsCurr(curr.getCurriculumId(),
                                              new Statistics(STAT_MASTERY_SORTED, Statistics.Type.FLOAT_LIST, sortedMasteryList.toString()),
                                              now));
            updateSet.add(statToAnalyticsCurr(curr.getCurriculumId(),
                                              new Statistics(STAT_MASTERY_MEAN, Statistics.Type.FLOAT, ukStatSvc.getMean(sortedMasteryList).toString()),
                                              now));
            updateSet.add(statToAnalyticsCurr(curr.getCurriculumId(),
                                              new Statistics(STAT_MASTERY_STD, Statistics.Type.FLOAT, ukStatSvc.getSTD(sortedMasteryList).toString()),
                                              now));
        }

        //save hash set to stat db
        logger.info("Saving: " + updateSet.size());
        statisticCurrRepo.saveAll(updateSet);
        logger.info("Saved: " + updateSet.size());
    }

    @Override
    public Statistics getStatistics(String currId, String statName) {
        StatsAnalyticsCurrKey id = new StatsAnalyticsCurrKey(currId, statName);
        Optional<StatsAnalyticsCurr> result = statisticCurrRepo.findById(id);
        
        if(!result.isPresent())
            return null;

        StatsAnalyticsCurr res = result.get();
        return new Statistics(res.getName(), Statistics.Type.getFromValue(res.getType()), res.getData());
    }

    @Override
    public Statistics getCoarseAverageStatistics(List<String> currIdList, String statName) {
        //Create ids from list
        List<StatsAnalyticsCurrKey> idList = new ArrayList<>();
        currIdList.forEach(currId -> idList.add(new StatsAnalyticsCurrKey(currId, statName)));

        //Query
        List<StatsAnalyticsCurr> resultList = (List<StatsAnalyticsCurr>) statisticCurrRepo.findAllById(idList);

        //If Result is null, return null
        if(resultList.size() == 0){ return null;  }

        //Create and return Statistics merged statistics
        //Merge the data. If any of the type is not float or int, throw error
        List<Float> averagedList = new ArrayList<>();
        for(StatsAnalyticsCurr result : resultList){
            //Get type of var
            Statistics.Type type = Statistics.Type.getFromValue(result.getType());

            //IF not float list nor float
            if(Statistics.Type.FLOAT_LIST != type &&  Statistics.Type.FLOAT != type){
                logger.warn("Invalid data type. Skipping." + result.toString());
                continue;
            }
            

            //Case --> float list
            if(type == Statistics.Type.FLOAT_LIST){
                //Convert data to List<Float>
                Type flistType = new TypeToken<List<Float>>(){}.getType();
                List<Float> sortedList = new Gson().fromJson(result.getData(), flistType);

                //For sorted List, add the averaged data to the averagedList
                float max = 0.0f;
                float min = 1.0f;
                int idx = 0;
                for(Float item: sortedList){
                    //If the List size is not populated yet
                    if(averagedList.size() < idx + 1){
                        averagedList.add(0.0f);
                    }

                    //Assume current size to uniform to all resultList
                    averagedList.set(idx, averagedList.get(idx) +(item / resultList.size()) );

                    if(item < min)
                        min = item;

                    if(item > max)
                        max = item;

                    idx++;
                }

                // logger.info(String.format("Stat log of [%s]. min:%f, max:%f", result.getCurrId(), min, max));
            }
            else if(type == Statistics.Type.FLOAT){
                Float value = Float.valueOf(result.getData());

                if(averagedList.size() == 0)
                    averagedList.add(0, 0.0f);

                averagedList.set(0, averagedList.get(0) + (value / resultList.size()));
            }
        }
        Collections.sort(averagedList);

        //NOTE: getting type from the first element might cause bugs

        if(Statistics.Type.getFromValue(resultList.get(0).getType()) == Statistics.Type.FLOAT_LIST)
            return new Statistics(statName, Statistics.Type.getFromValue(resultList.get(0).getType()), averagedList.toString());
        else
            return Statistics.builder()
                             .name(statName)
                             .type(Statistics.Type.getFromValue(resultList.get(0).getType()))
                             .data(averagedList.get(0).toString())
                             .build();
    }

    private StatsAnalyticsCurr statToAnalyticsCurr(String currId, Statistics stat, Timestamp now){
        return new StatsAnalyticsCurr(currId, stat.getName(), stat.getType().getValue(), stat.getData(), now);
    }


    @Override
    public Map<String, Float> getCurriculumMasteryOfUser(String userID) {
        //Prepare output object
        Map<String, Float> output = new HashMap<>();

        //Get all curriculums
        Iterable<Curriculum> currList = curriculumInfoRepo.findAll();

        //For all curriculum
        for(Curriculum curr: currList){
            //Get related UK lists.
            List<UserKnowledge> uknowList = userKnowledgeRepo.getByUserIDLikelyCurrID(userID, curr.getCurriculumId());

            //Class to help mastery stat calc
            MasteryStat stat = new MasteryStat();
            uknowList.forEach(uknow -> {
                Float mastery = uknow.getUkMastery();
                stat.addScore(mastery);
            });

            //Add the average value to output map
            output.put(curr.getCurriculumId(), stat.getAverage());
        }

        return output;
    }

    @Override
    public void setStatistics(String currID, String statname, Statistics stats) {
        // TODO Auto-generated method stub
        
    }
}

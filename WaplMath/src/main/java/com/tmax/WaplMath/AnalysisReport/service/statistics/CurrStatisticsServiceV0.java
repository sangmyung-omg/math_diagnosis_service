package com.tmax.WaplMath.AnalysisReport.service.statistics;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsCurr;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.statistics.StatisticCurrRepo;
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

    public void addScore(float score) {this.score += score; count++;}
    public float getAverage(){return this.score/count;}
    public float getScore(){return this.score;}
}

@Service("CurrStatisticsServiceV0")
public class CurrStatisticsServiceV0 implements CurrStatisticsServiceBase {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    StatisticCurrRepo statisticCurrRepo;

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    CurriculumInfoRepo curriculumInfoRepo;

    @Autowired
    UserKnowledgeRepo userKnowledgeRepo;

    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Override
    public void updateStatistics() {
        //Get all curriculums
        Iterable<Curriculum> currList = curriculumInfoRepo.findAll();

        //Create hashset
        Set<StatsAnalyticsCurr> updateSet = new HashSet<>();

        //For all curriculum
        for(Curriculum curr: currList){
            //Get related UK lists.
            List<UserKnowledge> uknowList = userKnowledgeRepo.getAllByLikelyCurrID(curr.getCurriculumId());

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
            Collections.sort(sortedMasteryList);


            //Prepare the updateSet for current curriculum and map it from the mastery map
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());


            //Add statistics to update set
            updateSet.add(statToAnalyticsCurr(curr.getCurriculumId(),
                                              new Statistics("mastery_sorted", Statistics.Type.FLOAT_LIST, sortedMasteryList.toString()),
                                              now));
            updateSet.add(statToAnalyticsCurr(curr.getCurriculumId(),
                                              new Statistics("mastery_mean", Statistics.Type.FLOAT_LIST, ukStatSvc.getMean(sortedMasteryList).toString()),
                                              now));
            updateSet.add(statToAnalyticsCurr(curr.getCurriculumId(),
                                              new Statistics("mastery_std", Statistics.Type.FLOAT_LIST, ukStatSvc.getSTD(sortedMasteryList).toString()),
                                              now));
        }

        //save hash set to stat db
        logger.info("Saving: " + updateSet.size());
        statisticCurrRepo.saveAll(updateSet);
        logger.info("Saved: " + updateSet.size());

    }

    private StatsAnalyticsCurr statToAnalyticsCurr(String currId, Statistics stat, Timestamp now){
        return new StatsAnalyticsCurr(currId, stat.getName(), stat.getType().getValue(), stat.getData(), now);
    }
}

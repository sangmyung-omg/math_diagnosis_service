package com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum;

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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsCurr;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsCurrKey;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.statistics.StatisticCurrRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics.Type;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.statistics.IScreamEduDataReader;
import com.tmax.WaplMath.AnalysisReport.util.statistics.StatisticsUtil;
// import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


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

@Data
@AllArgsConstructor
class CurrData {
    private String currID;
    private Float average;
}

@Slf4j
@Service("CurrStatisticsServiceV0")
public class CurrStatisticsServiceV0 implements CurrStatisticsServiceBase {
    @Autowired
    private StatisticCurrRepo statisticCurrRepo;

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    private CurriculumInfoRepo curriculumInfoRepo;

    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    private UserKnowledgeRepo userKnowledgeRepo;

    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Autowired
    private IScreamEduDataReader iScreamEduDataReader;

    @Override
    public void updateStatistics() {
        //Get all curriculums
        // List<Curriculum> currList = (List<Curriculum>)curriculumInfoRepo.findAll();
        List<String> currIDList = curriculumInfoRepo.getAllCurrID();

        //Prepare the updateSet for current curriculum and map it from the mastery map
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        //Create hashset
        Set<StatsAnalyticsCurr> updateSet = 
        currIDList.stream()
                    .parallel()
                    .flatMap(currID ->{
                        //currID
                        // String currID = curr.getCurriculumId();

                        //Get related UK lists.
                        List<UserKnowledge> uknowList = userKnowledgeRepo.getAllByLikelyCurrID(currID);

                        //Add i-scream edu data
                        if(iScreamEduDataReader.useIScreamData())
                            uknowList.addAll(iScreamEduDataReader.getByLikelyCurriculumID(currID));


                        //If no uknow is selected, then continue. Further steps are useless
                        if(uknowList == null || uknowList.size() == 0)
                            return Stream.empty();

                        //Create map <userid, masterystat> and userGradeMap
                        Map<String, MasteryStat> masteryMap = new HashMap<>();
                        Map<String, Integer> userGradeMap = new HashMap<>();
                        uknowList.stream()
                                //  .parallel()
                                .forEach(uknow -> {
                            String userID = uknow.getUserUuid();

                            //If there is no key (not initialized)
                            if(!masteryMap.containsKey(userID)){
                                masteryMap.put(userID, new MasteryStat());
                            }

                            //Add all mastery to stat per user
                            masteryMap.get(userID).addScore(uknow.getUkMastery());

                            //Build grade map
                            if(userGradeMap.containsKey(userID)){
                                return;
                            }
                            userGradeMap.put(userID, Integer.valueOf( uknow.getUser().getGrade()) );
                        });


                        //Create loop Set
                        Set<StatsAnalyticsCurr> allUserSet = getAllUserUpdateSet(currID, masteryMap, now);

                        //Get from grade 1~3
                        Set<StatsAnalyticsCurr> gradeUserSet =  
                                IntStream.range(1, 4)
                                        .parallel()
                                        .mapToObj(grade -> getSpecificGradeUpdateSet(currID, masteryMap, userGradeMap, grade, now))
                                        .flatMap(set -> set.stream())
                                        .collect(Collectors.toSet());
                        
                        //Merge and return stream
                        allUserSet.addAll(gradeUserSet);
                        
                        return allUserSet.stream();
                    })
                    .collect(Collectors.toSet());

        //save hash set to stat db
        log.info("Saving: " + updateSet.size());
        statisticCurrRepo.saveAll(updateSet);
        log.info("Saved: " + updateSet.size());
    }

    @Override
    public boolean updateStatistics(boolean isForced) {
        long result = statisticCurrRepo.count();
        if(result == 0)
            this.updateStatistics();

        return result == 0;
    }

    private Set<StatsAnalyticsCurr> getAllUserUpdateSet(String currID, Map<String, MasteryStat> masteryMap, Timestamp now){
        //Create hashset
        Set<StatsAnalyticsCurr> updateSet = new HashSet<>();

        //Create a List with mastery average then sort it --> create sorted list. Parallel optimization
        List<Float> sortedMasteryList = masteryMap.entrySet().stream()//.parallel()
                                                  .map(entry -> (float)entry.getValue().getAverage())
                                                  .collect(Collectors.toList());
        Collections.sort(sortedMasteryList); //sort

        //Add statistics to update set (All user)
        updateSet.add(currStatBuilder(currID, STAT_MASTERY_SORTED, Type.FLOAT_LIST, sortedMasteryList.toString(), now));
        updateSet.add(currStatBuilder(currID, STAT_MASTERY_MEAN, Type.FLOAT, ukStatSvc.getMean(sortedMasteryList).toString(), now));
        updateSet.add(currStatBuilder(currID, STAT_MASTERY_STD, Type.FLOAT, ukStatSvc.getSTD(sortedMasteryList).toString(), now));
        updateSet.add(currStatBuilder(currID, STAT_MASTERY_PERCENTILE_LUT, Type.FLOAT_LIST, StatisticsUtil.createPercentileLUT(sortedMasteryList, 1000).toString(), now));

        return updateSet;
    }

    private Set<StatsAnalyticsCurr> getSpecificGradeUpdateSet(String currID, 
                                                              Map<String, MasteryStat> masteryMap, 
                                                              Map<String, Integer> userGradeMap, 
                                                              Integer grade, 
                                                              Timestamp now){
        //Create hashset
        Set<StatsAnalyticsCurr> updateSet = new HashSet<>();

        //Create a List with mastery average then sort it --> create sorted list. Parallel optimization
        List<Float> sortedMasteryList = masteryMap.entrySet().stream()
                //   .parallel()
                  .filter(entry -> userGradeMap.get(entry.getKey()) == grade)
                  .map(entry -> (float)entry.getValue().getAverage())
                  .collect(Collectors.toList());
        Collections.sort(sortedMasteryList); //sort


        //Add statistics to update set (All user)
        updateSet.add(currStatBuilder(currID, STAT_MASTERY_SORTED + "_grade_" + grade, Type.FLOAT_LIST, sortedMasteryList.toString(), now));
        updateSet.add(currStatBuilder(currID, STAT_MASTERY_MEAN + "_grade_" + grade, Type.FLOAT, ukStatSvc.getMean(sortedMasteryList).toString(), now));
        updateSet.add(currStatBuilder(currID, STAT_MASTERY_STD + "_grade_" + grade, Type.FLOAT, ukStatSvc.getSTD(sortedMasteryList).toString(), now));
        updateSet.add(currStatBuilder(currID, STAT_MASTERY_PERCENTILE_LUT + "_grade_" + grade, Type.FLOAT_LIST, StatisticsUtil.createPercentileLUT(sortedMasteryList, 1000).toString(), now));

        return updateSet;
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
        List<StatsAnalyticsCurrKey> idList = currIdList.stream()
                                                .map(currId -> new StatsAnalyticsCurrKey(currId, statName))
                                                .collect(Collectors.toList());

        //Query
        List<Statistics> resultList = ((List<StatsAnalyticsCurr>) statisticCurrRepo.findAllById(idList))
                                                .stream().map(StatsAnalyticsCurr::toStatistics).collect(Collectors.toList());

        //If Result is null, return null
        if(resultList.size() == 0){ return null;  }

        List<Float> averagedList = new ArrayList<>();
        //Get stream array size from first element
        Statistics firstElem = resultList.get(0);
        boolean isFloatList = firstElem.getType() == Statistics.Type.FLOAT_LIST;

        if(isFloatList){
            //Create List<List<Float>> to save matrix
            List<List<Float>> floatListArray = resultList.stream().parallel()
                                                         .map(Statistics::getAsFloatList).collect(Collectors.toList());
            int arraySize = firstElem.getAsFloatList().size();
            averagedList = IntStream.range(0, arraySize).parallel().mapToObj(idx -> {
                //Add all and average
                double total = floatListArray.stream().parallel().mapToDouble(floatList -> floatList.get(idx))
                                                        .reduce(0.0, Double::sum);
                return (float)total / resultList.size();
            }).collect(Collectors.toList());

            try{
                Collections.sort(averagedList);
            }
            catch(Throwable e){
                log.debug(averagedList.toString());
            }
        }
        else {
            Float total = 0.0f;
            for(Statistics result: resultList){total += result.getAsFloat();}

            averagedList.add(total / resultList.size() );
        }
        

        //NOTE: getting type from the first element might cause bugs

        if(isFloatList)
            return new Statistics(statName, Statistics.Type.FLOAT_LIST, averagedList.toString());
        else
            return Statistics.builder()
                             .name(statName)
                             .type(Statistics.Type.FLOAT)
                             .data(averagedList.get(0).toString())
                             .build();
    }

    private StatsAnalyticsCurr currStatBuilder(String currId, String statname, Type type, String data, Timestamp ts){
        return StatsAnalyticsCurr.builder()
                                 .currId(currId)
                                 .name(statname)
                                 .type(type.getValue())
                                 .data(data)
                                 .lastUpdate(ts)
                                 .build();
    }


    @Override
    public Map<String, Float> getCurriculumMasteryOfUser(String userID) {
        //Get all curriculums
        // List<Curriculum> currList = (List<Curriculum>)curriculumInfoRepo.findAll();
        List<String> currIDList = curriculumInfoRepo.getAllCurrID();

        //For all curriculum
        Map<String, Float> output = 
            currIDList.stream()
                    .parallel()
                    .map(currID -> {
                        //Get related UK lists.
                        List<UserKnowledge> uknowList = userKnowledgeRepo.getByUserIDLikelyCurrID(userID, currID);

                        //Class to help mastery stat calc
                        MasteryStat stat = new MasteryStat();
                        uknowList.stream()
                                //  .parallel()
                                 .forEach(uknow -> {
                            Float mastery = uknow.getUkMastery();
                            stat.addScore(mastery);
                        });

                        //Add the average value to output map
                        return new CurrData(currID, stat.getAverage());
                    })
                    .collect(Collectors.toMap(CurrData::getCurrID, CurrData::getAverage));

        return output;
    }

    @Override
    public void setStatistics(String currID, String statname, Statistics stats) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        StatsAnalyticsCurr updateData =  currStatBuilder(currID, statname, stats.getType(),stats.getData(), now);

        statisticCurrRepo.save(updateData);
    }
}

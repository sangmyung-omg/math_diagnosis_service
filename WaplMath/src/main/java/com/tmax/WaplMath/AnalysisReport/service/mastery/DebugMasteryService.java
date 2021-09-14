package com.tmax.WaplMath.AnalysisReport.service.mastery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.tmax.WaplMath.AnalysisReport.dto.debug.TritonScoreDebugDTO;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.util.error.CommonErrorCode;
import com.tmax.WaplMath.Recommend.dto.mastery.TritonMasteryDTO;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepo;
import com.tmax.WaplMath.Recommend.service.mastery.v1.MasteryServiceV1;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;
import com.tmax.WaplMath.Recommend.util.MasteryAPIManager;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.problem.Problem;
import com.tmax.WaplMath.Common.model.problem.ProblemUkRel;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.repository.uk.UkRepo;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
class LRSDataHolder { 
    private Map<Integer, String> columnMap;
    private List<List<String>> dataList;

    @Builder.Default
    private Map<String, Integer> columnValueMap = new HashMap<>();

    public void setColumnMap(Map<Integer, String> map){
        this.columnMap = map;

        //Set value ==> integer map too
        map.keySet().forEach(key -> this.columnValueMap.put(map.get(key), key));
    }

    public void setDataList(List<List<String>> dataList){
        this.dataList = dataList;
    }

    public Integer getColumnIndex(String columnName) {
        if(this.columnValueMap.size() == 0){
            this.columnMap.keySet().forEach(key -> this.columnValueMap.put(this.columnMap.get(key), key));
        }
        
        return this.columnValueMap.get(columnName);
    }
}

@Service("DebugMasteryService")
@Slf4j
public class DebugMasteryService {
    @Autowired
    @Qualifier("RE-ProblemUkRelRepo")
    private ProblemUkRelRepo probUkRelRepo;

    @Autowired
    @Qualifier("RE-ProblemRepo")
    private ProblemRepo probRepo;

    @Autowired
    private MasteryAPIManager masteryAPIManager;

    @Autowired
    private ExamScopeUtil examScopeUtil;

    @Autowired
    private UkRepo ukRepo;

    public TritonScoreDebugDTO simulateScoreFromLrsCSV(String csvdata, Integer stride, String startSubSectionId, String endSubSectionId, List<String> excludeList){
        //Parse the data        
        LRSDataHolder dataHolder = parseCSV(csvdata);


        //Build FULL probIdList and correct array
        Integer sourceIdIdx = dataHolder.getColumnIndex("source_id");
        Integer isCorrectIdx = dataHolder.getColumnIndex("is_correct");
        List<String> probIdList = dataHolder.getDataList().stream().map(data -> data.get(sourceIdIdx)).collect(Collectors.toList());
        List<String> correctList = dataHolder.getDataList().stream().map(data -> data.get(isCorrectIdx)).collect(Collectors.toList());


        //get the terminal index's of each date
        Integer timestampIdx = dataHolder.getColumnIndex("timestamp");
        Map<String, Integer> dateIndexMap = new LinkedHashMap<>();
        IntStream.range(0, dataHolder.getDataList().size()).forEach(idx -> {
            String timestampStr = dataHolder.getDataList().get(idx).get(timestampIdx);
            dateIndexMap.put(timestampStr.substring(0, 10), idx);
        });


        //Cut the list into strides to get incremental score
        Map<String, Float> perDateMasteryProgression =  getPerDateMasteryProgression(dateIndexMap, probIdList, correctList, startSubSectionId, endSubSectionId, excludeList);
        Map<Integer, Float> perStrideMasteryProgression =  getPerStrideMasteryProgression(stride, probIdList, correctList, startSubSectionId, endSubSectionId, excludeList);


        //Build probCorr pair list
        List<Map<String,Object>> pairList = getPairList(probIdList, correctList);
        
        return TritonScoreDebugDTO.builder()
                                  .examStartCurrId(startSubSectionId)
                                  .examEndCurrId(endSubSectionId)
                                  .excludeCurrIdList(excludeList)
                                  .perDateMasteryProgression(perDateMasteryProgression)
                                  .perStrideMasteryProgression(perStrideMasteryProgression)
                                  .problemCorrectPairList(pairList)
                                  .build();
    }

    private List<Map<String,Object>> getPairList(List<String> probIdList, List<String> correctList){
        List<Map<String,Object>> output = new ArrayList<>();

        int size = probIdList.size();
        IntStream.range(0, size).forEach(idx -> {
            Map<String,Object> record = new HashMap<>();
            record.put("probId", probIdList.get(idx));
            record.put("correct", correctList.get(idx));
            output.add(record);
        });

        return output;
    }

    private Map<String, Float> getPerDateMasteryProgression(Map<String, Integer> dateIndexMap, List<String> probIdList, List<String> correctList, String startSubSectionId, String endSubSectionId, List<String> excludeList){
        //for each key of the date index map
        Map<String, Float> output = new LinkedHashMap<>();
        dateIndexMap.entrySet().stream().forEach(entry -> {
            Float score = getScore(probIdList.subList(0, entry.getValue() + 1), correctList.subList(0, entry.getValue() + 1), startSubSectionId, endSubSectionId, excludeList);
            output.put(entry.getKey(), score);
        });


        return output;
    }

    private Map<Integer, Float> getPerStrideMasteryProgression(Integer stride, List<String> probIdList, List<String> correctList, String startSubSectionId, String endSubSectionId, List<String> excludeList){

        int listSize = Math.min(probIdList.size(), correctList.size());
        int steps = (int)Math.ceil((double)listSize / (double)stride); //ciel to not leave any leftovers at the end of the array

        //Prepare output data
        Map<Integer, Float> output = new LinkedHashMap<>();

        IntStream.range(0, steps).forEach(idx -> {
            int currentStride = Math.min((idx+1) * stride, listSize);
            Float score = getScore(probIdList.subList(0, currentStride), correctList.subList(0, currentStride), startSubSectionId, endSubSectionId, excludeList);

            output.put(currentStride, score);
        });

        return output;
    }

    private Float getScore(List<String> probIdList, List<String> correctList, String startSubSectionId, String endSubSectionId, List<String> excludeList){
        TritonMasteryDTO result = updateMastery("debug", probIdList, correctList);

        Float score = getExamScopeStats(result.getMastery(), startSubSectionId, endSubSectionId, excludeList);
        return score;
    }


    private LRSDataHolder parseCSV(String csvString){
        //Split by return first --> line break;
        List<String> lineList = Arrays.asList(csvString.split("\n"));


        List<String> columnList = Arrays.asList(lineList.get(0).split(","));
        Map<Integer, String> columnMap = IntStream.range(0, columnList.size()).parallel().mapToObj(idx -> idx).collect(Collectors.toMap(idx -> idx, idx -> columnList.get(idx).toLowerCase()));

        List<List<String>> dataList = lineList.subList(1, lineList.size()).stream().parallel()
                                              .map(line -> Arrays.asList(line.split(",")))
                                              .collect(Collectors.toList());

        //make value index map
        Map<String, Integer> columnValueMap = columnMap.entrySet().stream().collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));

        //Sort datalist by timestamp
        Collections.sort(dataList, (a,b) -> {
            return a.get(columnValueMap.get("timestamp")).compareTo(b.get(columnValueMap.get("timestamp")));
        });


        return LRSDataHolder.builder().columnMap(columnMap).dataList(dataList).build();
    }

    private Float getExamScopeStats(Map<Integer, Float> masteryMap, String startSubSectionId, String endSubSectionId, List<String> excludeList){
        //Get the curr range data from the given examscope
        Set<String> currIdSet = examScopeUtil.getCurrIdList(startSubSectionId, endSubSectionId, excludeList)
                                             .stream().parallel().map(Curriculum::getCurriculumId).collect(Collectors.toSet());

        //Build Uk Id map from the masteryMap
        List<Integer> ukIdList = ( (List<Uk>)ukRepo.findAllById(masteryMap.keySet()) ).stream()
                                .filter(uk -> currIdSet.contains(uk.getCurriculumId())).map(Uk::getUkId).collect(Collectors.toList());

        //Sum all mastery of valid Uk
        List<Float> filteredMastery = masteryMap.entrySet().stream().filter(entry -> ukIdList.contains(entry.getKey())).map(entry -> entry.getValue()).collect(Collectors.toList());
        Float sum = filteredMastery.stream().reduce(0.0f, Float::sum);


        return sum / filteredMastery.size();
    }

    private TritonMasteryDTO updateMastery(String userId, List<String> probIdList, List<String> correctList) {
        log.info("Updating mastery for user " + userId);

        //Throw condition. Major error
        if (correctList.size() != probIdList.size()){
            throw new GenericInternalException(CommonErrorCode.GENERIC_ERROR,  "List size does not match");
        }
        
        //Pass condition check (any of the two lists' size == 0 or size doesn't match) => throw. must abort all other tasks
        if(probIdList.size() == 0 || correctList.size() == 0){
            throw new GenericInternalException(CommonErrorCode.GENERIC_ERROR, "list size mismatch");
        }

        //Convert probIdList to IntList
        List<Integer> probIdIntList = new ArrayList<>();
        for(String probId : probIdList){probIdIntList.add(Integer.parseInt(probId));}

        //Build List difficulty and UK id from probIDList
        // log.info("ProbList data: " + probIdList.toString());
        List<ProblemUkRel> probUKList = probUkRelRepo.getByProblemIDList(probIdIntList);

        //order list by probIDList order (Map<Prob ID, List<UK ID> >) 
        Map<Integer, List<Integer>> probIDUKMap = new HashMap<>();

        //Fill the map
        probUKList.forEach(puk -> {
            //Declare list placeholder
            List<Integer> ukList = null;

            //If nothing exists
            if(!probIDUKMap.containsKey(puk.getProbId())){
                //Create new List and add current ukId
                ukList = new ArrayList<>();
            }
            else {
                //Get the existing UKList
                ukList = probIDUKMap.get(puk.getProbId());
            }
            
            ukList.add(puk.getUkId());
            probIDUKMap.put(puk.getProbId(), ukList);
        });
        
        //Get the difficulty info of problems
        List<Problem> probDataList = probRepo.getProblemsByProbIdList(probIdIntList);
        
        //Build probId --> info map
        Map<Integer, String> probDiffMap = new HashMap<>();
        probDataList.forEach(prob->probDiffMap.put(prob.getProbId(), prob.getDifficulty()));

        //Using the map + probIDList(ordered), build the lists required for the mastery api manager
        List<String> ukIDList = new ArrayList<>();
        List<String> ukCorrectList = new ArrayList<>();
        List<String> diffList = new ArrayList<>();

        //Build the list
        int index = 0;
        for(String probId : probIdList){
            //Current correctness and diff level
            String correct = correctList.get(index++);
            String diffLevel = probDiffMap.get(Integer.parseInt(probId));

            //Get the UK List of the probId
            List<Integer> ukList = probIDUKMap.get(Integer.parseInt(probId));

            //Log
            log.debug("correct: " +  correct + ", diff: " + diffLevel + ", probId: " + probId);

            if(ukList == null){
                continue;
            }

            //For each ukList. push the values to list            
            ukList.forEach(uk -> {
                ukIDList.add(uk.toString());
                ukCorrectList.add(correct);
                diffList.add(diffLevel);
            });
        };


        String embeddingStr = "";

        //Measure the current mastery
        TritonMasteryDTO tritonMastery = masteryAPIManager.measureMasteryDTO(userId, ukIDList, ukCorrectList, diffList, embeddingStr);



        return tritonMastery;
    }
}

package com.tmax.WaplMath.AnalysisReport.service.diagnosis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.Common.util.lrs.ActionType;
import com.tmax.WaplMath.Common.util.lrs.LRSManager;
import com.tmax.WaplMath.Common.util.lrs.SourceType;
import com.tmax.WaplMath.Recommend.dto.mastery.TritonMasteryDTO;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepo;
import com.tmax.WaplMath.Recommend.repository.UkRepo;
import com.tmax.WaplMath.Recommend.util.MasteryAPIManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Common.dto.lrs.LRSStatementRequestDTO;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Common.model.problem.Problem;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.repository.problem.ProblemRepo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
class MasteryDataList {
    private List<String> ukIdList = new ArrayList<>();
    private List<String> corrList = new ArrayList<>();
    private List<String> diffList = new ArrayList<>();

    public MasteryDataList(List<MasteryData> dataList){
        dataList.forEach(data -> {
            this.ukIdList.add(data.getUkId());
            this.corrList.add(data.getCorr());
            this.diffList.add(data.getDiff());
        });
    }

    public void pushData(MasteryData data){
        this.ukIdList.add(data.getUkId());
        this.corrList.add(data.getCorr());
        this.diffList.add(data.getDiff());
    }

    public void pushData(String ukId, String corr, String diff){
        this.ukIdList.add(ukId);
        this.corrList.add(corr);
        this.diffList.add(diff);
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class MasteryData {
    private String ukId;
    private String corr;
    private String diff;
}

@Service("DiagnosisServiceV0")
@Slf4j
public class DiagnosisServiceV0 implements DiagnosisServiceBase{
    @Autowired
    private MasteryAPIManager masteryAPIManager;

    @Autowired
    private LRSManager lrsManager;

    @Autowired
    @Qualifier("UserStatisticsServiceV0")
    private UserStatisticsServiceBase userStatSvc;

    @Autowired
    @Qualifier("RE-ProblemUkRelRepo")
    ProblemUkRelRepo probUkRelRepo;

    @Autowired
    ProblemRepo problemRepo;

    @Autowired private ExamScopeUtil examScopeUtil;

    @Autowired private UkRepo ukRepo;

    @Autowired private UKStatisticsServiceBase ukStatSvc;

    @Override
    public PersonalScoreDTO getScore(String userID) {
        //If already exists return the existing score
        Statistics diagScoreStat = userStatSvc.getUserStatistics(userID, STAT_DIAGNOSIS_SCORE);
        if(diagScoreStat != null){
            float score = diagScoreStat.getAsFloat();
            log.info("Using existing diagnosis score {}: {}", userID, score);
            return PersonalScoreDTO.builder().score(100*score).percentile(100*getPercentile(userID, (double)score)).build();
        }


        List<LRSStatementResultDTO> statementList = getMostRecentDiagnosisStatements(userID);
        
        //Filter statements with invalid fields
        statementList = statementList.stream().filter(statement -> statement.getSourceId() != null && statement.getIsCorrect() != null).collect(Collectors.toList());

        //Get related problem infos -> build diff map
        List<Integer> probIdList = statementList.stream()
                                                .flatMap(statement->{
                                                    try{return Stream.of(Integer.valueOf(statement.getSourceId()));}
                                                    catch(Exception e){return Stream.empty();}
                                                }).collect(Collectors.toList());
        Map<Integer, String> probDiffMap = ((List<Problem>)problemRepo.findAllById(probIdList) ).stream()
                                                                      .collect(Collectors.toMap(Problem::getProbId, Problem::getDifficulty));

        //Build ukIDList and correctList
        MasteryDataList apiDatalist = new MasteryDataList(
            statementList.stream().flatMap(statement -> {
                Integer probId = null;
                try{probId = Integer.valueOf(statement.getSourceId());} catch(Exception e){return Stream.empty();}
                //Get ukId list for probId
                List<String> ukIdList = probUkRelRepo.findAllUkIdByProbId(probId).stream().map(id -> id.toString()).collect(Collectors.toList());
                String correct = statement.getIsCorrect() > 0 ? "true" : "false";
                String diff = probDiffMap.get(probId);

                //Create stream
                List<MasteryData> subdataList = ukIdList.stream().map(id -> MasteryData.builder().ukId(id).corr(correct).diff(diff).build())
                                                    .collect(Collectors.toList());
                
                return subdataList.stream();
            }).collect(Collectors.toList())
        );
        
        TritonMasteryDTO result = masteryAPIManager.measureMasteryDTO(userID, apiDatalist.getUkIdList(), apiDatalist.getCorrList(), apiDatalist.getDiffList(), "");
        if(result == null){return null;}

        //Get user's curriculum map
        List<String> currIdList = examScopeUtil.getCurrIdListOfScope(userID);
        Set<Integer> ukIdSet = currIdList.stream().flatMap(currID -> ((List<Uk>)ukRepo.findByCurriculumId(currID)).stream() )
                                         .map(Uk::getUkId).collect(Collectors.toSet());
        
        //Get mastery map
        Map<Integer, Float> masteryMap = result.getMastery();
        if(masteryMap == null){return null;}

        //From map, filter only uk in set (examscope range)
        List<Double> filteredMastery = masteryMap.entrySet().stream().filter(entry -> ukIdSet.contains(entry.getKey()) ).map(entry -> (double)entry.getValue()).collect(Collectors.toList());
        

        //Calc score and save to stat
        Double diagScore = filteredMastery.stream().reduce(0.0, Double::sum) / filteredMastery.size();
        userStatSvc.updateCustomUserStat(userID, STAT_DIAGNOSIS_SCORE, Statistics.Type.FLOAT, diagScore.toString());

        return PersonalScoreDTO.builder().score(100*diagScore.floatValue()).percentile(100*getPercentile(userID, diagScore)).build();
    }

    private Float getPercentile(String userID, Double score){
        //Check if examscore lut exists
        Statistics examscoreLUT = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_EXAMSCOPE_PERCENTILE_LUT);
        
        //If null. generate new one. TODO --> create a service that does this by user exam scope change event
        List<Float> masteryList = null;
        try {  masteryList = examscoreLUT.getAsFloatList(); }
        catch(Throwable e){log.error("Examscope LUT cannot be converted to float." + userID + examscoreLUT.toString()); return null;}
        
        return ukStatSvc.getPercentile(score.floatValue(), masteryList);
    }

    private List<LRSStatementResultDTO> getMostRecentDiagnosisStatements(String userID){
        //Get start and end statements to find the last bound
        List<LRSStatementResultDTO> boundStatementList = lrsManager.getStatementList(userID, 
                                                                                     Arrays.asList(ActionType.ENTER, ActionType.END), 
                                                                                     Arrays.asList(SourceType.DIAGNOSIS_SET, SourceType.DIAGNOSIS_SIMPLE_SET));
        //Reverse order sort by timestamp
        Collections.sort(boundStatementList, (a,b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        //Get the most recent bounding start/end set timestamp
        String endTimestamp = null;
        String startTimestamp = null;
        boolean isPrevEnd = false; //flag to check if last statement is end (actiontype)
        for(int idx = 0; idx < boundStatementList.size() - 1; idx++){
            //Get statement
            LRSStatementResultDTO statement = boundStatementList.get(idx);

            //If action is end, save data and check if next 
            if(statement.getActionType().equals(ActionType.END.getValue())){
                isPrevEnd = true;
                endTimestamp = statement.getTimestamp();
                continue;
            }


            //If prev statement is end and current is ENTER, save timestamp and break the whole loop
            if(isPrevEnd && statement.getActionType().equals(ActionType.ENTER.getValue())){
                startTimestamp = statement.getTimestamp();
                break;
            }

            isPrevEnd = false;
        }

        //Calculate mastery from lrs diagnosis
        //Get lrs statements with given timestamp range
        List<ActionType> actionTypeList = Arrays.asList(ActionType.SUBMIT);
        List<SourceType> sourceTypeList = SourceType.getDiagnosisOnly();

        List<LRSStatementResultDTO> result = lrsManager.getStatementList(LRSStatementRequestDTO.builder()
                                                                                               .userIdList(Arrays.asList(userID))
                                                                                               .sourceTypeList(sourceTypeList.stream().map(SourceType::getValue).collect(Collectors.toList()))
                                                                                               .actionTypeList(actionTypeList.stream().map(ActionType::getValue).collect(Collectors.toList()))
                                                                                               .dateFrom(startTimestamp)
                                                                                               .dateTo(endTimestamp)
                                                                                               .build()
                                                                                               );
        //Sort by timestamp
        Collections.sort(result, (a,b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        return result;
    }
}

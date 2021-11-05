package com.tmax.WaplMath.AnalysisReport.service.report;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataV2DTO;
import com.tmax.WaplMath.AnalysisReport.dto.report.UserPartMastery;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.type.TypeSimpleDTO;
import com.tmax.WaplMath.AnalysisReport.dto.type.TypeStatDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.typeknowledge.TypeKnowledgeScoreDTO;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsType;
import com.tmax.WaplMath.AnalysisReport.service.diagnosis.DiagnosisServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.score.ScoreServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.type.TypeStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreServiceBaseV0;
import com.tmax.WaplMath.AnalysisReport.service.typeknowledge.TypeKnowledgeServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.statistics.StatisticsUtil;
import com.tmax.WaplMath.Common.model.problem.ProblemType;
import com.tmax.WaplMath.Common.repository.problem.ProblemTypeRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportServiceV1 implements ReportServiceBaseV1 {
    @Autowired private ScoreServiceBase scoreSvc;
    @Autowired @Qualifier("DiagnosisServiceV0") private DiagnosisServiceBase diagSvc;
    @Autowired private TypeKnowledgeServiceBase typeKnowledgeSvc;
    @Autowired private UserStatisticsServiceBase userStatSvc;
    @Autowired private ProblemTypeRepo problemTypeRepo;
    @Autowired private TypeStatisticsServiceBase typeStatSvc;

    @Override
    public ReportDataV2DTO getReport(String userID, Integer limit, boolean topfirst, Set<String> excludeSet) {
        PersonalScoreDTO score = excludeSet.contains("score") ? null :  diagSvc.getScore(userID);
        PersonalScoreDTO waplscore = excludeSet.contains("waplscore") ? null :  scoreSvc.getWaplScore(userID, excludeSet);
        PersonalScoreDTO targetscore = excludeSet.contains("targetscore") ? null :  scoreSvc.getTargetScore(userID, excludeSet);
        GlobalStatisticDTO stats = excludeSet.contains("stats") ? null :  scoreSvc.getScoreStats(userID, excludeSet, 100);


        //Build type stat info
        //1) get type knowledge list from service and collect all type Ids
        List<TypeKnowledgeScoreDTO> knowledges = typeKnowledgeSvc.getAllOfUserSorted(userID, limit, !topfirst, excludeSet);
        Set<Integer> typeIdSet = knowledges.stream().map(TypeKnowledgeScoreDTO::getTypeID).collect(Collectors.toSet());
        Map<Integer, ProblemType> typeInfoMap = ((List<ProblemType>)problemTypeRepo.findAllById(typeIdSet) ).stream()
                                                    .collect(Collectors.toMap(ProblemType::getTypeId, p -> p));

        //2) Get the waplscore mastery map from the stat table
        Map<Integer, Float> rawWaplScoreMastery = new HashMap<>();
        Optional<Statistics> optionalStat = Optional.ofNullable(userStatSvc.getUserStatistics(userID, WaplScoreServiceBaseV0.STAT_WAPL_SCORE_MASTERY_TYPE_BASED));
        if(optionalStat.isPresent()){
            Statistics statWaplScoreMastery = optionalStat.get();
            
            //Convert to Map from json data
            Type type = new TypeToken<Map<Integer, Float>>(){}.getType();
            rawWaplScoreMastery = new Gson().fromJson(statWaplScoreMastery.getData(), type);
        }
        else {
            log.warn("No mastery found for waplscore type {}", userID);
        }
        //Filter only the typeIds in the set
        Map<Integer, Float> waplscoreMasteryMap = rawWaplScoreMastery.entrySet().stream()
                                                 .parallel()
                                                 .filter(entry -> typeIdSet.contains(entry.getKey()) )
                                                 .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        //Collect all type stats from db --> Split to each stat variables
        List<StatsAnalyticsType> rawTypeStats = typeStatSvc.getAllOfTypeIds(new ArrayList<>(typeIdSet));
        Map<Integer, Float> typeIdStdMap = rawTypeStats.stream().parallel()
                                                       .filter(stat -> stat.getName().equals(TypeStatisticsServiceBase.STAT_MASTERY_STD))
                                                       .collect(Collectors.toMap(StatsAnalyticsType::getTypeId, stat -> stat.toStatistics().getAsFloat()));
        Map<Integer, Float> typeIdMeanMap = rawTypeStats.stream().parallel()
                                                       .filter(stat -> stat.getName().equals(TypeStatisticsServiceBase.STAT_MASTERY_MEAN))
                                                       .collect(Collectors.toMap(StatsAnalyticsType::getTypeId, stat -> stat.toStatistics().getAsFloat()));
        // Map<Integer, Float> typeIdMeanMap = rawTypeStats.stream().parallel()
        //                                                .filter(stat -> stat.getName().equals(TypeStatisticsServiceBase.STAT_MASTERY_MEDIAN))
        //                                                .collect(Collectors.toMap(StatsAnalyticsType::getTypeId, stat -> stat.toStatistics().getAsFloat()));
        Map<Integer, List<Float> > typeIdPercentileMap = rawTypeStats.stream().parallel()
                                                       .filter(stat -> stat.getName().equals(TypeStatisticsServiceBase.STAT_MASTERY_PERCENTILE_LUT))
                                                       .collect(Collectors.toMap(StatsAnalyticsType::getTypeId, stat -> stat.toStatistics().getAsFloatList()));

        //Build the type list from the datas
        List<TypeStatDetailDTO> typeDataList = knowledges.stream()
                                                .map(knowledge -> {
                                                    Integer typeId = knowledge.getTypeID();
                                                    Float mastery = knowledge.getMastery().floatValue();
                                                    Float waplMastery = waplscoreMasteryMap.containsKey(typeId) ? waplscoreMasteryMap.get(typeId) : null;

                                                    List<Float> percentileMap = typeIdPercentileMap.get(typeId);

                                                    ProblemType typeInfo = typeInfoMap.get(typeId);
                                                    TypeSimpleDTO basic = TypeSimpleDTO.builder()
                                                                                        .id(typeId)
                                                                                        .currId(typeInfo.getCurriculumId())
                                                                                        .name(typeInfo.getTypeName())
                                                                                        .seq(typeInfo.getSequence())
                                                                                        .build();

                                                    PersonalScoreDTO kscore = PersonalScoreDTO.builder()
                                                                                              .score(100*mastery.floatValue())
                                                                                              .percentile(100* typeStatSvc.getPercentile(mastery, percentileMap))
                                                                                              .build();
                                                    PersonalScoreDTO kwaplscore = PersonalScoreDTO.builder()
                                                                                    .score(waplMastery != null ? 100* waplMastery : null)
                                                                                    .percentile(waplMastery != null ? 100* typeStatSvc.getPercentile(waplMastery, percentileMap) : null)
                                                                                    .build();

                                                    GlobalStatisticDTO kstats = 
                                                        GlobalStatisticDTO.builder()
                                                                        .std(typeIdStdMap.get(typeId))
                                                                        .mean(typeIdMeanMap.get(typeId))
                                                                        .percentile(StatisticsUtil.createPercentileLUT(percentileMap,100))
                                                                        .build();

                                                    return TypeStatDetailDTO.builder()
                                                                            .basic(basic)
                                                                            .score(kscore)
                                                                            .stats(kstats)
                                                                            .waplscore(kwaplscore)
                                                                            .build();
                                                })
                                                .collect(Collectors.toList());



        // Get part mastery from stat table
        Map<String, Float> partMasteryMap = null;
        Optional<Statistics>  partMasteryStatOpt = userStatSvc.getUserStatisticsOpt(userID, UserStatisticsServiceBase.STAT_USER_PART_MASTERY_MAP);
        if(partMasteryStatOpt.isPresent()){
            String partMastery = partMasteryStatOpt.get().getData();
            Type type = new TypeToken<Map<String, Float>>(){}.getType();
            partMasteryMap = new Gson().fromJson(partMastery, type);

            //multiply 100 to each float value. mastery -> score
            partMasteryMap = partMasteryMap.entrySet().stream()
                                           .collect(Collectors.toMap(entry -> entry.getKey(), entry -> 100*entry.getValue()));
        }

        Map<String, Float> waplPartMasteryMap = null;
        Optional<Statistics>  waplPartMasteryStatOpt = userStatSvc.getUserStatisticsOpt(userID, WaplScoreServiceBaseV0.STAT_WAPL_SCORE_PART_MASTERY);
        if(waplPartMasteryStatOpt.isPresent()){
            String partMastery = waplPartMasteryStatOpt.get().getData();
            Type type = new TypeToken<Map<String, Float>>(){}.getType();
            waplPartMasteryMap = new Gson().fromJson(partMastery, type);

            //multiply 100 to each float value. mastery -> score
            waplPartMasteryMap = waplPartMasteryMap.entrySet().stream()
                                           .collect(Collectors.toMap(entry -> entry.getKey(), entry -> 100*entry.getValue()));
        }

        return ReportDataV2DTO.builder()
                                .score(score)
                                .waplscore(waplscore)
                                .targetscore(targetscore)
                                .stats(stats)
                                .typeDataList(typeDataList)
                                .partMastery(UserPartMastery.builder().score(partMasteryMap).waplScore(waplPartMasteryMap).build())
                                .build();
    }
}

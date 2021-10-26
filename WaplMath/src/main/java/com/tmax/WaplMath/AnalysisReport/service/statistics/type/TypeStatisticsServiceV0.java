package com.tmax.WaplMath.AnalysisReport.service.statistics.type;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsType;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsTypeKey;
import com.tmax.WaplMath.AnalysisReport.repository.statistics.StatisticTypeRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.util.statistics.IScreamEduDataReaderV2;
import com.tmax.WaplMath.AnalysisReport.util.statistics.StatisticsUtil;
import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledge;
import com.tmax.WaplMath.Common.model.problem.ProblemType;
import com.tmax.WaplMath.Common.repository.knowledge.TypeKnowledgeRepo;
import com.tmax.WaplMath.Common.repository.problem.ProblemTypeRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("TypeStatisticsServiceV0")
@Slf4j
@Primary
public class TypeStatisticsServiceV0 implements TypeStatisticsServiceBase {
    @Autowired private StatisticTypeRepo statisticTypeRepo;
    @Autowired private ProblemTypeRepo problemTypeRepo;
    @Autowired private TypeKnowledgeRepo typeKnowledgeRepo;
    @Autowired private IScreamEduDataReaderV2 iScreamEduDataReaderV2;

    @Override
    public int updateAllStatistics() {
        //Get all type id list from db
        List<Integer> typeIdList = ( (List<ProblemType>)problemTypeRepo.findAll() )
                                        .stream().map(ProblemType::getTypeId).collect(Collectors.toList());

        //Get current timestamp(for uniform update time)
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        Set<StatsAnalyticsType> updateSet = typeIdList.stream().parallel()
                                                    .flatMap(id -> getdata(id, now))
                                                    .collect(Collectors.toSet());
        
        
        //Update the stat set to DB
        log.info("Saving sets: " + updateSet.size());
        statisticTypeRepo.saveAll(updateSet);
        log.info("Saved set");

        return 0;
    }

    private Stream<StatsAnalyticsType> getdata(Integer typeId, Timestamp ts){
        //Get related data + sort
        List<TypeKnowledge> typeKnowledges = typeKnowledgeRepo.findByTypeId(typeId);

        //Get from iscream v2
        if(iScreamEduDataReaderV2.useIScreamData())
            typeKnowledges.addAll(iScreamEduDataReaderV2.getByTypeID(typeId));

        List<Float> masteryList = typeKnowledges.stream()
                                                    .parallel().map(TypeKnowledge::getTypeMastery)
                                                    .collect(Collectors.toList());
        Collections.sort(masteryList);
        
        //Create each statistics
        Set<StatsAnalyticsType> updateSet = new HashSet<>();

        updateSet.add( statBuilder(typeId, STAT_MASTERY_MEAN, Statistics.Type.FLOAT, getMean(masteryList).toString(), ts) );
        updateSet.add( statBuilder(typeId, STAT_MASTERY_STD, Statistics.Type.FLOAT, getSTD(masteryList).toString(), ts) );
        updateSet.add( statBuilder(typeId, STAT_MASTERY_SORTED, Statistics.Type.FLOAT_LIST, masteryList.toString(), ts) );
        updateSet.add( statBuilder(typeId, STAT_MASTERY_PERCENTILE_LUT, Statistics.Type.FLOAT_LIST, StatisticsUtil.createPercentileLUT(masteryList, 1000).toString() ,ts) );

        return updateSet.stream();
    }

    private StatsAnalyticsType statBuilder(Integer typeId, String name, Statistics.Type type, String data, Timestamp ts){
        return StatsAnalyticsType.builder()
                                 .typeId(typeId)
                                 .name(name)
                                 .type(type.getValue())
                                 .data(data)
                                 .lastUpdate(ts)
                                 .build();
    }


    @Override
    public Statistics getTypeStatistics(Integer typeID, String statname) {
        Optional<StatsAnalyticsType> statOpt = statisticTypeRepo.findById(new StatsAnalyticsTypeKey(typeID, statname));

        if(!statOpt.isPresent()){
            log.warn("Stat {} for Type {} is not found. please check validity", statname, typeID);
            return null;
        }

        return statOpt.get().toStatistics();
    }

    @Override
    public Float getMean(List<Float> masteryList) {
        return masteryList.stream().reduce(0.0f, Float::sum) / masteryList.size();
    }

    @Override
    public Float getPercentile(Float score, List<Float> sortedMastery) {
        int index = 0;
        for(Float current: sortedMastery){
            if(score < current) break;
            index++;
        }

        return (float)index / sortedMastery.size();
    }

    @Override
    public Float getSTD(List<Float> masteryList) {
        Float mean = getMean(masteryList);

        Float var = masteryList.stream().parallel().map(mastery -> Math.pow(mastery - mean, 2))
                               .collect(Collectors.toList()) // list of (X - m)^2
                               .stream().reduce(0.0, Double::sum) // Sigma( (X-m)^2 )
                               .floatValue() / masteryList.size(); // variance
        return (float)Math.sqrt(var);
    }

    @Override
    public List<Float> getSorted(List<Float> masteryList) {
        //Copy
        List<Float> sortedList = new ArrayList<>(masteryList);
        Collections.sort(sortedList);
        return sortedList;
    }
}

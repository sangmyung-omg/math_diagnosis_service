package com.tmax.WaplMath.AnalysisReport.service.part;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.problem.ProblemType;
import com.tmax.WaplMath.Common.repository.problem.ProblemTypeRepo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
class PartMasteryData {
    private String part;
    private Float mastery;
}

@Service
@Slf4j
public class PartService {
    private ProblemTypeRepo problemTypeRepo;

    @Getter private Map<Integer, String> typePartMap = null;
    @Getter private Set<String> partSet = null;

    public PartService(ProblemTypeRepo repoInst){
        //Initialize repo
        this.problemTypeRepo = repoInst;

        //Initialize the part map for fast access
        log.info("Initializing part service");

        updateTypePartMap();
    }

    public void updateTypePartMap(){
        //Get all info from part
        List<ProblemType> allTypes = (List<ProblemType>)problemTypeRepo.findAll();

        //build the type --> part map
        this.typePartMap = allTypes.stream().parallel()
                                   .collect(
                                        Collectors.toMap(
                                            ProblemType::getTypeId, 
                                            type -> type.getCurriculum() != null ?  type.getCurriculum().getPart() : null //null check
                                        )
                                    );

        // Build part set
        this.partSet = allTypes.stream().parallel()
                                .filter(type -> type.getCurriculum() != null)
                                .map(ProblemType::getCurriculum)
                                .map(Curriculum::getPart)
                                .collect(Collectors.toSet());
    }

    @Scheduled(cron="0 0 0 * * *")
    public void scheduledUpdateCheck(){
        log.info("Updating in-memory part map.");
        updateTypePartMap();
    }

    public Map<String, Float> calculatePartMastery(Map<Integer, Float> userPartMastery){
        //Initialize with part set data
        Map<String, List<Float>> partMasteryListMap = this.partSet.stream().collect(Collectors.toMap(part -> part, part -> new ArrayList<Float>()));

        //For each user part mastery add to list
        userPartMastery.entrySet().stream()
                       //.parallel() //disabled due to possible thread race condition
                       .forEach(entry -> {
                           String currentPart = this.typePartMap.get(entry.getKey());

                           if(currentPart == null) return;

                           partMasteryListMap.get(currentPart).add(entry.getValue());
                       });

        
        //Build map with per part mastery
        Map<String, Float> outputMap = partMasteryListMap.entrySet().stream().parallel()
                                       .map(entry -> {
                                            List<Float> masteryList = entry.getValue();
                                            Float mastery = masteryList.stream().reduce(0.0f, Float::sum) / masteryList.size();

                                            return new PartMasteryData(entry.getKey(), mastery);
                                       })
                                       .collect(Collectors.toMap(PartMasteryData::getPart, PartMasteryData::getMastery));
                                                         
        return outputMap;
    }

}

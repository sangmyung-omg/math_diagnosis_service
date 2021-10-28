package com.tmax.WaplMath.AnalysisReport.service.typeknowledge;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tmax.WaplMath.AnalysisReport.dto.typeknowledge.TypeKnowledgeScoreDTO;
import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledge;
// import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledgeKey;
import com.tmax.WaplMath.Common.repository.knowledge.TypeKnowledgeRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import lombok.extern.slf4j.Slf4j;

@Service
// @Slf4j
public class TypeKnowledgeServiceV0 implements TypeKnowledgeServiceBase {
    @Autowired private TypeKnowledgeRepo typeKnowledgeRepo;


    @Override
    public List<TypeKnowledgeScoreDTO> getAllOfUser(String userID, Set<String> excludeSet) {
        //Find by user uuid
        List<TypeKnowledge> knowledges = typeKnowledgeRepo.findByUserUuid(userID);

        return knowledges.stream()
                         .parallel()
                         .map(knowledge -> TypeKnowledgeScoreDTO.builder()
                                                                .typeID(knowledge.getTypeId())
                                                                .mastery((double)knowledge.getTypeMastery())
                                                                .build()
                        )
                        .collect(Collectors.toList());
    }

    @Override
    public List<TypeKnowledgeScoreDTO> getByTypeIdList(String userID, List<Integer> typeIDs) {
        return getByTypeIdList(userID, typeIDs, new HashSet<>());
    }

    @Override
    public List<TypeKnowledgeScoreDTO> getByTypeIdList(String userID, List<Integer> typeIDs, Set<String> excludeSet) {
        //Find by user uuid (Get all) then filter
        // List<TypeKnowledge> knowledges = typeKnowledgeRepo.findByUserUuid(userID)
        //                                                   .stream().filter(knowledge -> typeIDs.contains(knowledge.getTypeId()) )
        //                                                   .collect(Collectors.toList());
        List<TypeKnowledge> knowledges = typeKnowledgeRepo.findByUserUuidAndTypeIds(userID, typeIDs);

        return knowledges.stream()
                            .parallel()
                            .map(knowledge -> TypeKnowledgeScoreDTO.builder()
                                                                    .typeID(knowledge.getTypeId())
                                                                    .mastery((double)knowledge.getTypeMastery())
                                                                    .build()
                            )
                            .collect(Collectors.toList());
    }

    @Override
    public List<TypeKnowledgeScoreDTO> getAllOfUserSorted(String userID, Integer limit, boolean orderAsc,
            Set<String> excludeSet) {
        
        //Get mastery list of user using given condition
        List<TypeKnowledge> knowledges = null;
        if(limit < 0){ //no limit -> all data
            knowledges = orderAsc ? typeKnowledgeRepo.findByUserIDSortedAsc(userID) : typeKnowledgeRepo.findByUserIDSortedDesc(userID);
        }
        else {
            knowledges = orderAsc ? typeKnowledgeRepo.findByUserIDSortedLimitedAsc(userID, limit) : typeKnowledgeRepo.findByUserIDSortedLimitedDesc(userID, limit);
        }

        return knowledges.stream().parallel()
                         .map(knowledge -> TypeKnowledgeScoreDTO.builder().typeID(knowledge.getTypeId()).mastery(knowledge.getTypeMastery().doubleValue()).build())
                         .collect(Collectors.toList());
    }
}
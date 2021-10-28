package com.tmax.WaplMath.AnalysisReport.service.typeknowledge;

import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.dto.typeknowledge.TypeKnowledgeScoreDTO;

public interface TypeKnowledgeServiceBase {
    public List<TypeKnowledgeScoreDTO> getByTypeIdList(String userID, List<Integer> typeID);
    public List<TypeKnowledgeScoreDTO> getByTypeIdList(String userID, List<Integer> typeID, Set<String> excludeSet);

    public List<TypeKnowledgeScoreDTO> getAllOfUser(String userID, Set<String> excludeSet);

    public List<TypeKnowledgeScoreDTO> getAllOfUserSorted(String userID, Integer limit, boolean orderAsc, Set<String> excludeSet);
}

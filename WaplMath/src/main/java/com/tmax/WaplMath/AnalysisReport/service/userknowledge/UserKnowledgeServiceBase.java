package com.tmax.WaplMath.AnalysisReport.service.userknowledge;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeDetailDTO;

public interface UserKnowledgeServiceBase {
    public UkUserKnowledgeDetailDTO getByUkId(String userID, Integer ukID);
    public List<UkUserKnowledgeDetailDTO> getByUkIdList(String userID, List<Integer> ukID);
}

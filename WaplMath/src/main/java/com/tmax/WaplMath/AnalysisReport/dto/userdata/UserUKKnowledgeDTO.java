package com.tmax.WaplMath.AnalysisReport.dto.userdata;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeScoreDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUKKnowledgeDTO {
    private String userID;
    private List<UkUserKnowledgeScoreDTO> ukKnowledgeList;   
}

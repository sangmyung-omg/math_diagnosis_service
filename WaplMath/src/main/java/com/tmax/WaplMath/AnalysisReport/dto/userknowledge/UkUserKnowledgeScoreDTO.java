package com.tmax.WaplMath.AnalysisReport.dto.userknowledge;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UkUserKnowledgeScoreDTO {
    private Integer ukID;
    private Double mastery;
}

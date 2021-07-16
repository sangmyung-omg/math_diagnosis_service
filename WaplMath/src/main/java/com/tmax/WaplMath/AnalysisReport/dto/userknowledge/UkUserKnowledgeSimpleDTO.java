package com.tmax.WaplMath.AnalysisReport.dto.userknowledge;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.uk.UkSimpleDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UkUserKnowledgeSimpleDTO {
    private UkSimpleDTO ukInfo;
    private PersonalScoreDTO mastery;
}

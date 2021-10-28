package com.tmax.WaplMath.AnalysisReport.dto.typeknowledge;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.type.TypeSimpleDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypeKnowledgeDetailDTO {
    private TypeSimpleDTO typeInfo;
    private PersonalScoreDTO mastery;
    private PersonalScoreDTO waplscore;
    private GlobalStatisticDTO stats;
}

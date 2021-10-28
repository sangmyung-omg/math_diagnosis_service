package com.tmax.WaplMath.AnalysisReport.dto.typeknowledge;

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
public class TypeKnowledgeSimpleDTO {
    private TypeSimpleDTO typeInfo;
    private PersonalScoreDTO mastery;
}

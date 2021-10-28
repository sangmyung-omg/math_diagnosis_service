package com.tmax.WaplMath.AnalysisReport.dto.typeknowledge;

// import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TypeKnowledgeScoreDTO {
    private Integer typeID;
    private Double mastery;
}

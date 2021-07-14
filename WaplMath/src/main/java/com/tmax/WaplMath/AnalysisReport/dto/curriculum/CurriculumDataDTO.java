package com.tmax.WaplMath.AnalysisReport.dto.curriculum;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeDetailDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumDataDTO {
    private List<CurriculumDataDetailDTO> currDataList;
    private List<UkUserKnowledgeDetailDTO> ukKnowledgeList;
}

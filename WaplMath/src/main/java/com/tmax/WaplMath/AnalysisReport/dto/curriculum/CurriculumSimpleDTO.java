package com.tmax.WaplMath.AnalysisReport.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumSimpleDTO {
    private String type;
    private String name;
    private String id;
    private Integer seq;
}

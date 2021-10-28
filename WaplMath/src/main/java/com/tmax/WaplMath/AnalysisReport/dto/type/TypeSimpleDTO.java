package com.tmax.WaplMath.AnalysisReport.dto.type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypeSimpleDTO {
    private Integer id;
    private String name;
    private Integer seq;
    private String currId;
}

package com.tmax.WaplMath.AnalysisReport.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomStatDataDTO {
    private Integer pick;
    private Integer unknown;
    private Integer notserious;
    private Integer notfocused;
}

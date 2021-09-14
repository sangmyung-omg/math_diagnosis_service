package com.tmax.WaplMath.AnalysisReport.dto.debug;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TritonScoreDebugDTO {
    
    private String examStartCurrId;
    private String examEndCurrId;
    private List<String> excludeCurrIdList;

    //Mastery progression by date
    private Map<String, Float> perDateMasteryProgression;
    private Map<Integer, Float> perStrideMasteryProgression;
}

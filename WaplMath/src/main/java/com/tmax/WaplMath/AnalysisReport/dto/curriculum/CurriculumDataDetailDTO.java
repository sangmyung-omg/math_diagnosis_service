package com.tmax.WaplMath.AnalysisReport.dto.curriculum;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumDataDetailDTO {
    private CurriculumSimpleDTO basic;
    private PersonalScoreDTO score;
    private PersonalScoreDTO waplscore;
    private GlobalStatisticDTO stats;
    private List<Integer> ukIdList;
}

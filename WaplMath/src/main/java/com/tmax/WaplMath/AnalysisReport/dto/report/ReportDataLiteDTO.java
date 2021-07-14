package com.tmax.WaplMath.AnalysisReport.dto.report;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumSimpleDTO;
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
public class ReportDataLiteDTO {
    private PersonalScoreDTO score;
    private PersonalScoreDTO waplscore;
    private PersonalScoreDTO targetscore;
    private GlobalStatisticDTO stats;
    private List<CurriculumSimpleDTO> currList;
}

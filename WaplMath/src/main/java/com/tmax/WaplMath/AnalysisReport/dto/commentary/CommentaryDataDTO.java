package com.tmax.WaplMath.AnalysisReport.dto.commentary;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumSimpleDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.CorrectRateDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.SolveSpeedDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentaryDataDTO {
    private PersonalScoreDTO score;
    private SolveSpeedDTO solveSpeed;
    private CorrectRateDTO correctRate;
    private List<CurriculumSimpleDTO> lowPartList;
    private List<CurriculumSimpleDTO> highPartList;
}

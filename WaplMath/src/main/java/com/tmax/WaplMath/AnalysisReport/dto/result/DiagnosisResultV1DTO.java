package com.tmax.WaplMath.AnalysisReport.dto.result;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.LevelDiagnosisRecordDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisResultV1DTO {
    private SummaryReportDTO summary;
    private List<ChapterDetailDTO> chapterDetailList;
    private LevelDiagnosisRecordDTO levelDiagnosisRecord;
    private WAPLScoreDTO waplScore;
}

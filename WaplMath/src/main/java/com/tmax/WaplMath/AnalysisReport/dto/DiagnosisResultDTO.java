package com.tmax.WaplMath.AnalysisReport.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisResultDTO {
    private SummaryReportDTO summary;
    private List<ChapterDetailDTO> chapterDetailList;
    private LevelDiagnosisRecordDTO levelDiagnosisRecord;
}

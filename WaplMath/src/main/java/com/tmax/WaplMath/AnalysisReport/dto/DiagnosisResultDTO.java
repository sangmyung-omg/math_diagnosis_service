package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisResultDTO {
    private SummaryReportDTO summary;
    private ChapterIDListDTO chapterIDList;
    private LevelDiagnosisRecordDTO levelDiagnosisRecord;
}

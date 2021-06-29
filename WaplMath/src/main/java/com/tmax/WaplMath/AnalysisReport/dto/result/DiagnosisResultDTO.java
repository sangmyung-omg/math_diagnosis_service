package com.tmax.WaplMath.AnalysisReport.dto.result;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.LevelDiagnosisRecordDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;

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

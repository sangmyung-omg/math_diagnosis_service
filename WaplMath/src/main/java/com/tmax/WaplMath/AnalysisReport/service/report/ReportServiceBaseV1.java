package com.tmax.WaplMath.AnalysisReport.service.report;

import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataV2DTO;

public interface ReportServiceBaseV1 {
    public ReportDataV2DTO getReport(String userID, Integer limit, boolean topfirst,  Set<String> excludeSet);
}

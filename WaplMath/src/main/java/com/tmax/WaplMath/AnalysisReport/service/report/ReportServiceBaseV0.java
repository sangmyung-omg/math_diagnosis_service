package com.tmax.WaplMath.AnalysisReport.service.report;

import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.report.ReportDataLiteDTO;

public interface ReportServiceBaseV0 {
    public ReportDataDTO getReport(String userID, Set<String> excludeSet);
    public ReportDataLiteDTO getReportLite(String userID, Set<String> excludeSet);    
}

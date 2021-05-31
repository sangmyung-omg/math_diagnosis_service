package com.tmax.WaplMath.AnalysisReport.service.summary;

import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;

public interface SummaryServiceBase {
    SummaryReportDTO getSummaryOfUser(String userID);
}

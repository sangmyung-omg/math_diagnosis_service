package com.tmax.WaplMath.AnalysisReport.service.summary;

import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;

/**
 * Summary service base interface
 * @author Jonghyun Seong
 */
public interface SummaryServiceBase {
    SummaryReportDTO getSummaryOfUser(String userID);
}

package com.tmax.WaplMath.AnalysisReport.service.summary;

import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;

import org.springframework.stereotype.Service;

/**
 * Summary service dummy implementation
 * @author Jonghyun Seong
 */
@Service("SummaryServiceDummy")
public class SummaryServiceDummy implements SummaryServiceBase{
    @Override
    public SummaryReportDTO getSummaryOfUser(String userID) {
        SummaryReportDTO output =  new SummaryReportDTO();
        output.setScore(100);
        output.setPercentile(81.0);

        return output;
    }
}

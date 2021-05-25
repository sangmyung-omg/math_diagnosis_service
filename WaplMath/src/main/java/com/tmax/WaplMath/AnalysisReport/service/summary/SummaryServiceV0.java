package com.tmax.WaplMath.AnalysisReport.service.summary;

import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("v0")
public class SummaryServiceV0 implements SummaryServiceBase{
    @Override
    public SummaryReportDTO getSummaryOfUser(String userID) {
        //FIXME: Dummy output
        SummaryReportDTO output =  new SummaryReportDTO();
        output.setScore(100);
        output.setPercentile(81.0);

        return output;
    }
}

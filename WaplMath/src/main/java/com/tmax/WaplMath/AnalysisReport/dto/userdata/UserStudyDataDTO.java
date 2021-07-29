package com.tmax.WaplMath.AnalysisReport.dto.userdata;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.BasicProblemStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.CustomStatDataDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStudyDataDTO {
    private String userID;
    private BasicProblemStatDTO basic;
    private CustomStatDataDTO custom;
}

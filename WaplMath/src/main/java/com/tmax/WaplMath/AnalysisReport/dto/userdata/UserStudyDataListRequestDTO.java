package com.tmax.WaplMath.AnalysisReport.dto.userdata;

import java.util.List;

import com.tmax.WaplMath.Common.dto.TimeRange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStudyDataListRequestDTO {
    private List<String> userIDList;
    private TimeRange timeRange;
    private Object rule;
}

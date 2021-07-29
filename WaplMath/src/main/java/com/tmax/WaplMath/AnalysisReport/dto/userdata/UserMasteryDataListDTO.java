package com.tmax.WaplMath.AnalysisReport.dto.userdata;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.uk.UkDataDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserMasteryDataListDTO {
    private List<UserUKKnowledgeDTO> userDataList;
    private List<UkDataDTO> ukDataList;
}

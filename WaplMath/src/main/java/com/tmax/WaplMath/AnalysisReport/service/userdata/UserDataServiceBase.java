package com.tmax.WaplMath.AnalysisReport.service.userdata;

import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserMasteryDataListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserStudyDataDTO;

public interface UserDataServiceBase {
    List<UserStudyDataDTO> getStudyStatList(List<String> userIDList, String from, String until, Set<String> excludeSet);
    List<UserStudyDataDTO> getStudyStatList(List<String> userIDList, Set<String> excludeSet);
    UserMasteryDataListDTO getUserMasteryDataList(List<String> userIDList, Set<String> excludeSet);
}

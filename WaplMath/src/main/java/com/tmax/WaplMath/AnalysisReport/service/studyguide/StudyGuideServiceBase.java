package com.tmax.WaplMath.AnalysisReport.service.studyguide;

import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;

public interface StudyGuideServiceBase {
    StudyGuideDTO getStudyGuideOfUser(String userID);
}

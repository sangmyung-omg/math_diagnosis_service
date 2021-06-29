package com.tmax.WaplMath.AnalysisReport.service.studyguide;

import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;

/**
 * Studyguide service base interface
 * @author Jonghyun Seong
 */
public interface StudyGuideServiceBase {
    StudyGuideDTO getStudyGuideOfUser(String userID);
}

package com.tmax.WaplMath.AnalysisReport.service.chapter;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;

public interface ChapterServiceBase {

    List<ChapterDetailDTO> getAllChapterListOfUser(String userID);

    List<ChapterDetailDTO> getAllChapterListOfUserChapterOnly(String userID);
    List<ChapterDetailDTO> getAllChapterListOfUserSectionOnly(String userID);
    List<ChapterDetailDTO> getAllChapterListOfUserSubSectionOnly(String userID);

    List<ChapterDetailDTO> getChapterListOfUserInRange(String userID, String range, String subrange);
}

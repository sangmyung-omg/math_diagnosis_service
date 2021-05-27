package com.tmax.WaplMath.AnalysisReport.service.chapter;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;

public interface ChapterServiceBase {

    List<ChapterDetailDTO> getAllChapterListOfUser(String userID);

    List<ChapterDetailDTO> getAllChapterListOfUserChapterOnly(String userID);
    List<ChapterDetailDTO> getAllChapterListOfUserSectionOnly(String userID);
    List<ChapterDetailDTO> getAllChapterListOfUserSubSectionOnly(String userID);

    
    List<ChapterDetailDTO> getSpecificChapterListOfUser(String userID, ChapterIDListDTO chapterIDList, String saturation);
    List<ChapterDetailDTO> getSpecificChapterListOfUser(String userID, ChapterIDListDTO chapterIDList);

    List<ChapterDetailDTO> getSpecificChapterListOfUser(String userID, List<String> chapterIDList, String saturation);
    List<ChapterDetailDTO> getSpecificChapterListOfUser(String userID, List<String> chapterIDList);
}

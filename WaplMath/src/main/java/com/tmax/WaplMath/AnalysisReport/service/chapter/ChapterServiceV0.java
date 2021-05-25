package com.tmax.WaplMath.AnalysisReport.service.chapter;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
@Qualifier("v0")
public class ChapterServiceV0 implements ChapterServiceBase{
    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUser(String userID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ChapterDetailDTO> getSpecificChapterListOfUser(String userID, ChapterIDListDTO chapterIDList) {
        // TODO Auto-generated method stub
        return null;
    }
}

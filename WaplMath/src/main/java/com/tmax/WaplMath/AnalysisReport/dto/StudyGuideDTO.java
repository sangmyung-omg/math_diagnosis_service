package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.Data;
import java.util.List;

@Data
public class StudyGuideDTO {
    private List<ChapterDetailDTO> chapterDetailList;
    private String commentary;
}
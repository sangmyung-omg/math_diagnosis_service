package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.Data;
import java.util.List;

@Data
class StudyGuideDTO {
    private List<String> chapterIDList;
    private String commentary;
}
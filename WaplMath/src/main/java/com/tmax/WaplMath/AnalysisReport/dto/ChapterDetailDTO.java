package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.Data;

@Data
class ChapterDetailDTO {
    private String id;
    private String type;
    private String name;
    private String imagePath;
    private SkillStatDTO skillData;
    private List<UKDetailDTO> ukDetailList;
}
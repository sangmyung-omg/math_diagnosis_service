package com.tmax.WaplMath.AnalysisReport.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChapterDetailDTO {
    private String id;
    private String type;
    private int sequence;
    private String name;
    private String imagePath;
    private SkillStatDTO skillData;
    private List<UKDetailDTO> ukDetailList;
}
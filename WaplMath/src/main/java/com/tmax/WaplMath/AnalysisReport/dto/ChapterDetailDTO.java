package com.tmax.WaplMath.AnalysisReport.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChapterDetailDTO {
    private String id;
    private String type;
    private int sequence;
    private String name;
    private String imagePath;
    private SkillStatDTO skillData;
    private List<UKDetailDTO> ukDetailList;
}
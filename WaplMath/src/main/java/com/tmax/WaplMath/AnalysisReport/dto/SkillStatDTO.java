package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkillStatDTO {
    private double user;
    private double userpercentile;
    private double waplscore;
    private double waplscorepercentile;
    private double average;
    private double top10Tier;
    private double globalstd;
}
package com.tmax.WaplMath.AnalysisReport.util.statistics;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class DataTable {
    private String username;
    private List<Float> masteryList;
}
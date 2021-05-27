package com.tmax.WaplMath.AnalysisReport.dto;

import java.util.List;

import lombok.Data;

@Data
public class LRSGetStatementDTO {
    private String dateFrom;
    private String dateTo;
    private String recentStatementNum;

    private List<String> sourceTypeList;
    private List<String> userIdList;
    private List<String> verbTypeList;
    
}

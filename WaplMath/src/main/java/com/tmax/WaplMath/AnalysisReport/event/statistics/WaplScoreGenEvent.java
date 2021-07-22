package com.tmax.WaplMath.AnalysisReport.event.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WaplScoreGenEvent {
    private String userID;
    private boolean forceGen;

    public WaplScoreGenEvent(String userID) {
        this.userID = userID;
        this.forceGen = false;
    }
}

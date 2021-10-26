package com.tmax.WaplMath.AnalysisReport.util.statistics;

import java.util.Map;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class UserData {
    @Builder.Default
    private Float mastery = 0.0f;

    @Builder.Default
    private int count = 0;

    @Builder.Default
    private Map<String, Float> map = new HashMap<>();

    public void add(String userID, Float mastery) {
        this.count++;
        this.mastery += mastery;
        this.map.put(userID, mastery);
    }

    public Float getAverage() {
        return this.mastery / this.count;
    }

    public Float getMastery(String userID){
        return this.map.get(userID);
    }

    public Map<String, Float> getMap() {
        return this.map;
    }

    //Method to update passively
    public void updateMastery() {
        this.count = map.size();
        this.mastery = 0.0f; //clear

        for(Map.Entry<String, Float> entry : this.map.entrySet()){
            this.mastery += entry.getValue();
        }
    }
}
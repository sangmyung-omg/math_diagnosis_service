package com.tmax.WaplMath.AnalysisReport.util.statistics;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
class Mastery implements Serializable {
    @Builder.Default
    private Float mastery = 0.0f;

    @Builder.Default
    private int count = 0;

    @Builder.Default
    private Map<Integer, Float> map = new HashMap<>();

    public void add(Integer ukID, Float mastery) {
        this.count++;
        this.mastery += mastery;
        this.map.put(ukID, mastery);
    }

    //Method to update passively
    public void updateMastery() {
        this.count = map.size();
        this.mastery = 0.0f; //clear

        for(Map.Entry<Integer, Float> entry : this.map.entrySet()){
            // System.out.println(entry);
            if(entry == null || entry.getValue() == null){
                log.warn("Entry is null. {} {}", entry.getKey(), entry.getValue());
                continue;
            }
            
            this.mastery += entry.getValue();
        }
    }

    public Float getAverage() {
        return this.mastery / this.count;
    }

    public Float getMastery(Integer ukID){
        return this.map.get(ukID);
    }

    public Map<Integer, Float> getMap() {
        return this.map;
    }
}
package com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum;

import lombok.Data;

@Data
public class MasteryStat {
    private Float score = 0.0f;
    private int count = 0;

    public MasteryStat(){}

    public void addScore(Float score) {
        count++;

        //If nan. add zero
        if(score.isNaN()){
            return;
        }

        this.score += score; 
    }
    public float getAverage(){
        if(count == 0)
            return 0.0f;

        return this.score/count;        
    }
    public float getScore(){return this.score;}
}
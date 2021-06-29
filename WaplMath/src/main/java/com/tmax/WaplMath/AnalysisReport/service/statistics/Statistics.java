package com.tmax.WaplMath.AnalysisReport.service.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

//Struct for internal statistic return use.
@Data
@AllArgsConstructor
public class Statistics {
    private String name;
    private Type type;
    private String data;

    public enum Type{
        FLOAT("float"),
        STRING("string"),
        FLOAT_LIST("float_list")
        ;

        @Getter private String value;
        private Type(String value){
            this.value = value;
        }
    }
}
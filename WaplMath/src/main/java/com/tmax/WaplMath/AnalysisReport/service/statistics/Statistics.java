package com.tmax.WaplMath.AnalysisReport.service.statistics;

import java.util.HashMap;
import java.util.Map;

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
        FLOAT_LIST("float_list"),
        JSON("json")
        ;

        @Getter private String value;
        private Type(String value){
            this.value = value;
        }

        //LUT to get type from
        private static final Map<String, Type> valueMap = new HashMap<>();
        public static Type getFromValue(String value) {
            return valueMap.get(value);
        }

        static {
            for(Type type: Type.values()){
                valueMap.put(type.getValue(), type);
            }
        }

        
    }
}
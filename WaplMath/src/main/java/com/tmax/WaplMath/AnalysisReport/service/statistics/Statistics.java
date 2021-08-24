package com.tmax.WaplMath.AnalysisReport.service.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

//Struct for internal statistic return use.
@Data
@AllArgsConstructor
@Builder
public class Statistics {
    private String name;
    private Type type;
    private String data;

    public enum Type{
        INT("int"),
        FLOAT("float"),
        STRING("string"),
        FLOAT_LIST("float_list"),
        STRING_LIST("string_list"),
        INT_LIST("int_list"),
        JSON("json")
        ;

        @Getter private String value;
        private Type(String value){
            this.value = value;
        }

        public String toString(){
            return this.value;
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

    public Float getAsFloat(){
        return Float.valueOf(data);
    }

    public Integer getAsInt(){
        return Integer.valueOf(data);
    }

    public List<Float> getAsFloatList(){
        java.lang.reflect.Type floatType = new TypeToken<List<Float>>(){}.getType();
        return new Gson().fromJson(this.data, floatType);
    }

    public List<Integer> getAsIntegerList(){
        java.lang.reflect.Type intType = new TypeToken<List<Integer>>(){}.getType();
        return new Gson().fromJson(this.data, intType);
    }

    public List<String> getAsStringList(){
        java.lang.reflect.Type strType = new TypeToken<List<String>>(){}.getType();
        return new Gson().fromJson(this.data, strType);
    }

    public JsonElement getAsJson(){
        return JsonParser.parseString(this.data);
    }
}
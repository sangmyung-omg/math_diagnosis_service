package com.tmax.WaplMath.Recommend.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagnosisPart {
    public static final List<String> diagnosisPart = Arrays.asList("수와 연산", "문자와 식", "함수", "기하", "확률과 통계");
    public static final Map<String, String> partMap = new HashMap<String, String>();

    static {
        partMap.put("1", "수와 연산");
        partMap.put("2", "문자와 식");
        partMap.put("3", "함수");
        partMap.put("4", "기하");
        partMap.put("5", "확률과 통계");
    }
}

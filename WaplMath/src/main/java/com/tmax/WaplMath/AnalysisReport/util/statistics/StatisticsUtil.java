package com.tmax.WaplMath.AnalysisReport.util.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Jonghyun Seong
 * @since 2021-07-07
 * 
 * Class to help calc in statistics service
 */
public class StatisticsUtil {
    public static List<Float> createPercentileLUT(List<Float> sortedList, int steps){
        //Declare output list
        List<Float> output = new ArrayList<>(steps);

        //sortedList size
        double inOutStepRatio = (double)sortedList.size()/steps;

        //Create a 0 ~ steps LUT
        IntStream.range(0, steps).forEachOrdered(idx -> {
            int scaledIdx = (int)Math.floor(idx * inOutStepRatio);
            output.set(idx, sortedList.get(scaledIdx));
        });

        return output;
    }
}

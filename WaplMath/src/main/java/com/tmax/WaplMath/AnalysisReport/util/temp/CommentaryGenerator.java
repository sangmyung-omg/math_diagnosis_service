package com.tmax.WaplMath.AnalysisReport.util.temp;

import java.util.Set;

import lombok.Getter;

public class CommentaryGenerator {

    private static final String TEMPLATE_SCORE = "와플수학 AI 분석 결과 %s 학생의 현재 수준은 %d점으로 상위 %d%%에요.";
    private static final String TEMPLATE_SPEED_CORRECT_RATE = " 문제 풀이 속도가 %s, %s 정답률을 보이고 있어요.";
    private static final String TEMPLATE_HIGH_LOW_PART = " %s에 대한 이해도는 높지만 %s 영역에서 보충 학습이 필요해요.";
    private static final String TEMPLATE_LAST = " 아래 단원별 분석 결과에서 현재 실력과 보충이 필요한 단원을 자세히 확인할 수 있어요.";


    enum Speed {
        SLOW("다소 느리고"),
        MODERATE("양호하고"),
        FAST("빠른 편이고"),
        VERY_FAST("매우 빠르고");

        @Getter
        private String message;

        private Speed(String message){this.message = message;}
    }

    enum CorrectRate {
        LOW("낮은"),
        MODERATE("다소 낮은"),
        AVERAGE("보통 수준의"),
        HIGH("높은");

        @Getter
        private String message;

        private CorrectRate(String message){this.message = message;}
    }

    public static String createFromData(String userName, 
                          Double score, 
                          Double percentile, 
                          Double speed, 
                          Double correctRate,
                          Set<String> highPartList,
                          Set<String> lowPartList){
        String output = "";
        

        //Add score template
        output += String.format(TEMPLATE_SCORE, userName, (int)Math.floor(score), (int)Math.floor(percentile));

        //Add speed
        if(speed != null && correctRate != null){
            String speedStr = "";
            String correctStr = "";

            //speed
            if(speed < 0.5)
                speedStr = Speed.SLOW.getMessage();
            else if(speed < 0.8)
                speedStr = Speed.MODERATE.getMessage();
            else if(speed < 0.9)
                speedStr = Speed.FAST.getMessage();
            else
                speedStr = Speed.VERY_FAST.getMessage();

            //Correct
            if(correctRate < 0.5)
                correctStr = CorrectRate.LOW.getMessage();
            else if(correctRate < 0.8)
                correctStr = CorrectRate.MODERATE.getMessage();
            else if(correctRate < 0.9)
                correctStr = CorrectRate.AVERAGE.getMessage();
            else
                correctStr = CorrectRate.HIGH.getMessage();

            output += String.format(TEMPLATE_SPEED_CORRECT_RATE, speedStr, correctStr);
        }

        //Add highLow
        if(lowPartList != null && highPartList != null && lowPartList.size() > 0 && highPartList.size() > 0){
            String lowListStr = "";
            String highListStr = "";
            for(String str: lowPartList){lowListStr += str + ", ";}
            for(String str: highPartList){highListStr += str + ", ";}

            output += String.format(TEMPLATE_HIGH_LOW_PART, lowListStr.substring(0, lowListStr.length() - 2), highListStr.substring(0, highListStr.length() - 2));
        }

        //last
        output += TEMPLATE_LAST;
        
        
        return output;
    }
}

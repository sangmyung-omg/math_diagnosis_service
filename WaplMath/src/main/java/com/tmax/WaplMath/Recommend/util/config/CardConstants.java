package com.tmax.WaplMath.Recommend.util.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class CardConstants {

  // Hyperparameter
  public static final Integer MAX_CARD_PROB_NUM = 20;
  
  public static final Integer MAX_TYPE_CARD_NUM = 4;
  public static final Integer MIN_TYPE_CARD_PROB_NUM = 2;
  public static final Integer MAX_TYPE_CARD_PROB_NUM = 5;

  public static final Integer SUPPLE_CARD_TYPE_NUM = 3;
  public static final Integer SUPPLE_CARD_PROB_NUM_PER_TYPE = 2;
  public static final Integer SUPPLE_CARD_TYPE_THRESHOLD = 30;
  
  public static final Integer SECTION_TEST_CARD_HIGH_PROB = 5;
  public static final Integer SECTION_TEST_MIDDLE_PROB = 9;
  public static final Integer SECTION_TEST_LOW_PROB = 6;
  
  public static final float MASTERY_HIGH_THRESHOLD = 0.7f;
  public static final float MASTERY_LOW_THRESHOLD = 0.4f;

  public static final Integer AVERAGE_PROB_ESTIMATED_TIME = 180;
  
  // Constant
  public static final String TYPE_CARD_TYPESTR = "type";
  public static final String SUPPLE_CARD_TYPESTR = "supple";
  public static final String SECTION_TEST_CARD_TYPESTR = "section_test";
  public static final String CHAPTER_TEST_CARD_TYPESTR = "chapter_test ";
  public static final String ADDTL_SUPPLE_CARD_TYPESTR = "addtl_supple";
  public static final String SECTION_EXAM_CARD_TYPESTR = "section_exam";
  public static final String FULL_SCOPE_EXAM_CARD_TYPESTR = "full_scope_exam";
  public static final String TRIAL_EXAM_CARD_TYPESTR = "trial_exam";
  
  public static final String SUPPLE_CARD_TITLE_FORMAT = "보충 유형 %d개 복습";
  public static final String ADDTL_SUPPLE_CARD_TITLE_FORMAT = "유형 %d개 복습";
  public static final String TRIAL_EXAM_CARD_TITLE_FORMAT = "중학교 %s학년 %s학기 %s 대비";

  public static final String LRS_SOURCE_TYPE_POSTFIX = "_question";

  public enum Difficulty {
    
    상("high", SECTION_TEST_CARD_HIGH_PROB),
    중("middle", SECTION_TEST_MIDDLE_PROB),
    하("low", SECTION_TEST_LOW_PROB);

    
    private @Getter String diffEng;
    private @Getter Integer probNums;


    private Difficulty(String diffEng, Integer probNums){
      this.diffEng = diffEng;
      this.probNums = probNums;
    }

    // orderList (high=0, middle=1, low=2) 순서 대로 Difficulty 리스트 리턴
    public static List<Difficulty> getDiffListByOrder(Integer[] orderList){
      List<Difficulty> diffList = new ArrayList<>();
      
      for (Integer order: orderList){
        for (Difficulty diff: Difficulty.values()){
          if (diff.ordinal() == order)
            diffList.add(diff);
        }
      }

      return diffList;
    }

  }
  
}

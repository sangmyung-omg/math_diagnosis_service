package com.tmax.WaplMath.Recommend.util.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public enum Difficulty implements CardConstants {
    
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

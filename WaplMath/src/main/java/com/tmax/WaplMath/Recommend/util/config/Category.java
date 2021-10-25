package com.tmax.WaplMath.Recommend.util.config;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

public enum Category {
  TYPE("유형"),
  SIMPLE_DIAG("간단"),
  IN_DEPTH_DIAG("꼼꼼"),
  TEXTBOOK("교과서"),
  PAST("기출"),
  TEST("모의고사");

  private @Getter String category;

  private Category(String category){
    this.category = category;
  }

  public static List<Category> getTypeCardPrioirty(){
    return Arrays.asList(TYPE, IN_DEPTH_DIAG, PAST, TEXTBOOK, TEST);
  }
  
  public static List<Category> getSuppleCardPrioirty(){
    return Arrays.asList(TYPE, IN_DEPTH_DIAG, PAST, TEXTBOOK, TEST);
  }
  
  public static List<Category> getTestCardPrioirty(){
    return Arrays.asList(TYPE, IN_DEPTH_DIAG, PAST, TEXTBOOK, TEST);
  }
  
  public static List<Category> getExamCardPrioirty(){
    return Arrays.asList(PAST, TEXTBOOK, TEST, TYPE, IN_DEPTH_DIAG);
  }

  public static List<Category> getTrialExamCardPrioirty(){
    return Arrays.asList(TEST, PAST, TEXTBOOK, TYPE, IN_DEPTH_DIAG);
  }
}

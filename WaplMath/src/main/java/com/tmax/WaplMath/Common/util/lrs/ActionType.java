package com.tmax.WaplMath.Common.util.lrs;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import lombok.Getter;

public enum ActionType {
    SUBMIT("submit"),
    START("start");

    @Getter
    private String value;

    private ActionType(String value){
      this.value = value;
    }

    public static List<String> getAllActionTypes(){
      return Stream.of(ActionType.values()).map(s -> s.getValue()).collect(Collectors.toList());
    }
}

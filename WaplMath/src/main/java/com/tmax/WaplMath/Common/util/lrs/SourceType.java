package com.tmax.WaplMath.Common.util.lrs;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import lombok.Getter;

public enum SourceType {
    DIAGNOSIS("diagnosis"),
    DIAGNOSIS_SIMPLE("diagnosis_simple"),
    TYPE_QUESTION("type_question"), 
    SUPPLE_QUESTION("supple_question"), 
    SECTION_TEST_QUESTION("section_test_question"),
    CHAPTER_TEST_QUESTION("chapter_test_question"),
    ADDTL_SUPPLE_QUESTION("addtl_supple_question"),
    SECTION_EXAM_QUESTION("section_exam_question"),
    FULL_SCOPE_EXAM_QUESTION("full_scope_exam_question"),
    TRIAL_EXAM_QUESTION("trial_exam_question"),
    RETRY_QUESTION("retry_question"),
    WRONG_ANSWER_QUESTION("wrong_answer_question"),
    STARRED_QUESTION("starred_question");

    @Getter
    private String value;

    private SourceType(String value){
      this.value = value;
    }

    public static List<String> getAllSourceTypes(){
      return Stream.of(SourceType.values()).map(s -> s.getValue()).collect(Collectors.toList());
    }

    public static List<String> getDiagnosisOnly() {
      return Stream.of(DIAGNOSIS, DIAGNOSIS_SIMPLE).map(s -> s.getValue()).collect(Collectors.toList());
    }
}

package com.tmax.WaplMath.AnalysisReport.model.problem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class ProblemCurriculum {
    
    @Id
    @Column(name="PROB_ID")
    private int probID;

    @Column(name="CURRICULUM_ID")
    private String curriculumID;

    @Column(name="SEQUENCE")
    private String typeSeq; //유형 순서

    @Column(name="TYPE_NAME")
    private String typeName;

    @Column(name="SCHOOL_TYPE")
    private String schoolType;

    @Column(name="GRADE")
    private String grade;

    @Column(name="SEMESTER")
    private String semester;

    @Column(name="CHAPTER")
    private String chapter;

    @Column(name="SECTION")
    private String section;

    @Column(name="SUB_SECTION")
    private String subSection;

    @Column(name="PART")
    private String part;

}

package com.tmax.WaplMath.AnalysisReport.model.curriculum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.lang.Nullable;

import lombok.Data;

@Data
@Entity
public class UserMasteryCurriculum {
    
    private String userUuid;

    @Id
    @Column(name="UK_ID")
    private int ukId;
    private String ukName;

    @Column(name="UK_MASTERY")
    private double ukMastery;

    @Column(name="CURRICULUM_ID")
    private String curriculumId;
    private String chapter;

    @Nullable
    private String section;

    @Nullable
    private String subSection;

    private int curriculumSequence;
}

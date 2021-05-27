package com.tmax.WaplMath.AnalysisReport.model.curriculum;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class UserMasteryCurriculum {
    @Id
    private String userUuid;

    private int ukId;
    private String ukName;
    private String curriculumId;
    private String section;
    private int curriculumSequence;
}

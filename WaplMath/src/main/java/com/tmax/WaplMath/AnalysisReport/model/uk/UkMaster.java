package com.tmax.WaplMath.AnalysisReport.model.uk;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class UkMaster {
    @Id
    private int ukId;

    private String ukName;
    private String ukDescription;
    private String curriculumId;    
}

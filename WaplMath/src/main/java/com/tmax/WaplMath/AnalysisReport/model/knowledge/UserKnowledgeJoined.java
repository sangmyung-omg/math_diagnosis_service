package com.tmax.WaplMath.AnalysisReport.model.knowledge;

// import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class UserKnowledgeJoined {
    
    private String userUuid;
    

    @Id
    private int ukId;
    private String ukMastery;
    private String ukName;
    private String ukDescription;

    private String curriculumId;
    
    private String probId;
}

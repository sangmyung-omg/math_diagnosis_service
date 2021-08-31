package com.tmax.WaplMath.AnalysisReport.dto.userdata;

// import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLRSRecordSimpleDTO {
    // private String userID;
    private Integer pID;
    private Long dur;
    private String corr;
    private String diff;
    private String time;

    private String srcType;
}

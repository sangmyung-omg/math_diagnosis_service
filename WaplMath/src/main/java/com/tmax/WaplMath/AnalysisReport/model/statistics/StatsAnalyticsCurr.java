package com.tmax.WaplMath.AnalysisReport.model.statistics;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(StatsAnalyticsCurrKey.class)
@Table(name="STATS_ANALYTICS_CURR")
public class StatsAnalyticsCurr {
    @Id
    private String currId;

    @Id
    private String name;

    private String type;

    @Lob
    private String data;

    private Timestamp lastUpdate;
    private Timestamp validUntil;

    public Statistics toStatistics(){
        return Statistics.builder()
                            .name(this.name)
                            .type(Statistics.Type.getFromValue(this.type))
                            .data(this.data)
                            .build();
    }
}

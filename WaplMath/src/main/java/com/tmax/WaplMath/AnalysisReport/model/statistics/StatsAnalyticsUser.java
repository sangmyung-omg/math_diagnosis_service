package com.tmax.WaplMath.AnalysisReport.model.statistics;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(StatsAnalyticsUserKey.class)
@Table(name="STATS_ANALYTICS_USER")
public class StatsAnalyticsUser {
    @Id
    private String userId;

    @Id
    private String name;

    private String type;

    @Lob
    private String data;

    private Timestamp lastUpdate;
    private Timestamp validUntil;
}

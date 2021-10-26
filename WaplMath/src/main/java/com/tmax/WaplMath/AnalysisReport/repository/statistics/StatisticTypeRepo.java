package com.tmax.WaplMath.AnalysisReport.repository.statistics;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsType;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsTypeKey;

import org.springframework.data.repository.CrudRepository;

public interface StatisticTypeRepo extends CrudRepository<StatsAnalyticsType, StatsAnalyticsTypeKey> {
    
}

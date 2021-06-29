package com.tmax.WaplMath.AnalysisReport.repository.statistics;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUk;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUkKey;

import org.springframework.data.repository.CrudRepository;

public interface StatisticUKRepo extends CrudRepository<StatsAnalyticsUk, StatsAnalyticsUkKey>{
    
}

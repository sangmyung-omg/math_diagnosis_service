package com.tmax.WaplMath.AnalysisReport.repository.statistics;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsCurr;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsCurrKey;

import org.springframework.data.repository.CrudRepository;

public interface StatisticCurrRepo extends CrudRepository<StatsAnalyticsCurr, StatsAnalyticsCurrKey> {
    
}

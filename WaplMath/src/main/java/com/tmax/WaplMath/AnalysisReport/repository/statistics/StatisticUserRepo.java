package com.tmax.WaplMath.AnalysisReport.repository.statistics;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUser;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUserKey;

import org.springframework.data.repository.CrudRepository;

public interface StatisticUserRepo extends CrudRepository<StatsAnalyticsUser, StatsAnalyticsUserKey> {

}

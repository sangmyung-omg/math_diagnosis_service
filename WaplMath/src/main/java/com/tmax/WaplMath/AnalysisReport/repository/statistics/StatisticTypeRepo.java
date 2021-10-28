package com.tmax.WaplMath.AnalysisReport.repository.statistics;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsType;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsTypeKey;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StatisticTypeRepo extends CrudRepository<StatsAnalyticsType, StatsAnalyticsTypeKey> {
    @Query("select stats from StatsAnalyticsType stats where stats.typeId in :typeIds")
    public List<StatsAnalyticsType> findByTypeIds(@Param("typeIds") List<Integer> typeIds);
}

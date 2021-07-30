package com.tmax.WaplMath.AnalysisReport.repository.statistics;

import javax.transaction.Transactional;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUser;
import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUserKey;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StatisticUserRepo extends CrudRepository<StatsAnalyticsUser, StatsAnalyticsUserKey> {

    @Modifying
    @Transactional
    @Query("delete from StatsAnalyticsUser where userId=:userID")
    public void deleteAllOfUser(@Param("userID") String userID);
}

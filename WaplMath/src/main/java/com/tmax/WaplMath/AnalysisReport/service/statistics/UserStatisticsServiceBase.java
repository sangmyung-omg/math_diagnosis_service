package com.tmax.WaplMath.AnalysisReport.service.statistics;

public interface UserStatisticsServiceBase {
    /**
     * method to update designated user's statistics
     * @param userID
     */
    public void updateSpecificUser(String userID);

    public void updateAllUsers();
}

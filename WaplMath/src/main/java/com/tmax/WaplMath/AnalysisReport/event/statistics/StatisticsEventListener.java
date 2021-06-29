package com.tmax.WaplMath.AnalysisReport.event.statistics;

import com.tmax.WaplMath.Recommend.event.mastery.MasteryChangeEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("StatisticsEventListener")
public class StatisticsEventListener {
    

    /**
     * Handler to call services that run on mastery change
     * @param event
     */
    @EventListener
    public void handleMasteryUpdateEvent(MasteryChangeEvent event) {
        
    }
}

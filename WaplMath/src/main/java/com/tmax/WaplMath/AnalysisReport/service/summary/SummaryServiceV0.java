package com.tmax.WaplMath.AnalysisReport.service.summary;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;
import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;
import com.tmax.WaplMath.AnalysisReport.repository.legacy.curriculum.UserCurriculumRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Summary service v0 implementation
 * @author Jonghyun Seong
 */
@Slf4j
@Service("SummaryServiceV0")
@Primary
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(value="file:${external-config.url}/application.properties", ignoreResourceNotFound=true),
})
public class SummaryServiceV0 implements SummaryServiceBase{
    @Value("${external-config.url}")
    private String externalConfigURL;
    
    @Autowired
    private UserCurriculumRepo currRepo;

    @Override
    public SummaryReportDTO getSummaryOfUser(String userID) {
        SummaryReportDTO output =  new SummaryReportDTO();


        //Get all data from 중3-1
        List<UserMasteryCurriculum> dbData = currRepo.getUserCurriculumWithCurrRange(userID, "중등-중3-1학%");
        Map<Integer, Double> ukMasteryMap = new HashMap<>();

        for(UserMasteryCurriculum row: dbData){
            if(ukMasteryMap.containsKey(row.getUkId()))
                continue;

            ukMasteryMap.put(row.getUkId(), row.getUkMastery());
        }

        

        //==== Calculate percentile of everyone ==========

        //get Average uk mastery data
        Path path = null;
        String filepathSuffix = "statistics/uk_min_max.json";
        try {path = ResourceUtils.getFile("classpath:" + filepathSuffix).toPath();}
        catch (FileNotFoundException e) {log.warn("File not found internally: "+ filepathSuffix);}

        try {path = ResourceUtils.getFile("file:" + externalConfigURL + "/" + filepathSuffix).toPath();} 
        catch (FileNotFoundException e) {log.error("File alno not found externally.: "+ filepathSuffix);}

        FileReader reader = null;
        try {
            reader = new FileReader(path.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonObject result = (JsonObject)JsonParser.parseReader(reader);
        


        // === UK List loop
        //
        int count = 0; 
        double userScore, minScore, maxScore;
        userScore = minScore = maxScore = 0.0;
        for(Map.Entry<Integer, Double> entry : ukMasteryMap.entrySet()){
            count++;
            
            //Step 1: 본인 평균 계산
            userScore += entry.getValue();

            //Step 2: 전체 유저 평균 계산
            minScore += result.get("min").getAsJsonObject().get(entry.getKey().toString()).getAsDouble();
            maxScore += result.get("max").getAsJsonObject().get(entry.getKey().toString()).getAsDouble();
        }
        

        //NaN은 NaN으로 두어서 에러가 났음을 알림
        output.setScore(userScore*100.0 / count);
        output.setPercentile( 100*( 100 * (userScore - minScore)/(maxScore - minScore) ) / count);

        return output;
    }
}

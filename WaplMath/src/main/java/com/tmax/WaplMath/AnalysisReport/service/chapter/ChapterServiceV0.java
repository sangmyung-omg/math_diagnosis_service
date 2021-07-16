package com.tmax.WaplMath.AnalysisReport.service.chapter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SkillStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UKDetailDTO;
import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;
import com.tmax.WaplMath.AnalysisReport.repository.legacy.curriculum.UserCurriculumRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Data
@NoArgsConstructor
class ValueCount {
    private double masteryTotal = 0.0;
    private int currentCount = 0;
    private String name = "";
    private String type = "null";
    private int sequence = 0;

    private Map<Integer, String> ukList = new HashMap<>();

    public void increase(){
        this.currentCount++;
    }

    public double getAverageMastery() {
        if(this.currentCount == 0)
            return 0;

        return this.masteryTotal / (double)this.currentCount;
    }

    public void insertUK(Integer ukID, String name){
        //Skip if exists
        if(this.ukList.containsKey(ukID))
            return;
        this.ukList.put(ukID, name);
    }
}



/**
 * Chapter service v0 interface
 * @author Jonghyun Seong
 */
@Slf4j
@Service("ChapterServiceV0")
@Primary
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(value="file:${external-config.url}/application.properties", ignoreResourceNotFound=true),
})
public class ChapterServiceV0 implements ChapterServiceBase{
    @Value("${external-config.url}")
    private String externalConfigURL;
    
    @Autowired
    UserCurriculumRepo currRepo;

    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUser(String userID) {
        //Get the mastery level and curriculum section info 
        List<UserMasteryCurriculum> mid2result= currRepo.getUserCurriculum(userID);

        return this.createListFromDBResult(mid2result, "none");
    }

    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUserChapterOnly(String userID) {
        //Get the mastery level and curriculum section info (중2 강제는 임시)
        List<UserMasteryCurriculum> mid2result= currRepo.getUserCurriculumWithCurrRange(userID, "중등-중2%");

        return this.createListFromDBResult(mid2result, "chapter");
    }

    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUserSectionOnly(String userID) {
        //Get the mastery level and curriculum section info
        List<UserMasteryCurriculum> mid2result= currRepo.getUserCurriculum(userID);

        return this.createListFromDBResult(mid2result, "section");
    }

    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUserSubSectionOnly(String userID) {
        //Get the mastery level and curriculum section info
        List<UserMasteryCurriculum> mid2result= currRepo.getUserCurriculum(userID);

        return this.createListFromDBResult(mid2result, "subsection");
    }


    private List<ChapterDetailDTO> createListFromDBResult(List<UserMasteryCurriculum> dbresult, String saturation) {
        //Create a hashmap to save the average of mastery per section
        Map<String, ValueCount> currSectionUKAvg = new HashMap<>();

        Map<Integer, Double> ukSkillMap = new HashMap<>();

        //Sum all mastery per 
        for(UserMasteryCurriculum data : dbresult){
            //Get currid            
            String currID = data.getCurriculumId();

            // //cut the last 3 digits (대단원 id clipping)
            // if(currID.length() > 6)
            //     currID = currID.substring(0, currID.length() - 6);
            if(saturation.equals("chapter")){
                currID = currID.substring(0, 11);
            }
            else if(saturation.equals("section")){
                currID = currID.substring(0, 14);
            } 
            else if(saturation.equals("subsection")){
                currID = currID.substring(0, 17);
            }
            
            ValueCount val = currSectionUKAvg.containsKey(currID) ? currSectionUKAvg.get(currID) : new ValueCount();
            if(saturation.equals("chapter")){
                val.setType("대단원");
            }
            else if(saturation.equals("section")){
                val.setType("중단원");
            } 
            else if(saturation.equals("subsection")){
                val.setType("소단원");
            }

            val.increase(); //Count 증가
            val.setMasteryTotal(val.getMasteryTotal() + data.getUkMastery()); //전체 mastery 증가
            val.setName(data.getSection()); //대단원 명 입력
            val.insertUK(data.getUkId(), data.getUkName()); //단원에 포함됨 UK id 리스트 구축
            val.setSequence(data.getCurriculumSequence());

            //단원 종류 판단
            // System.out.println(data.toString());
            // if(data.getSubSection() != null){
            //     val.setType("소단원");
            // }
            // else if(data.getSubSection() != null){
            //     currID = currID.substring(0, currID.length() - 3);
            //     val.setType("중단원");
            // }
            // else {
            //     currID = currID.substring(0, currID.length() - 6);
            //     val.setType("대단원");
            // }

            // update
            
            
            currSectionUKAvg.put(currID,val); 

            ukSkillMap.put(data.getUkId(), data.getUkMastery());
        }

        // System.out.println(currSectionUKAvg);

        List<ChapterDetailDTO> outputList = new ArrayList<ChapterDetailDTO>();

        for(Map.Entry<String, ValueCount> entry: currSectionUKAvg.entrySet()){
            ChapterDetailDTO data = new ChapterDetailDTO();
            data.setId(entry.getKey());
            data.setImagePath("/dummy.png");
            data.setName(entry.getValue().getName());
            
            //평균 스킬 계산
            SkillStatDTO skill = new SkillStatDTO();
            skill.setAverage(calculateSkill(50, entry.getValue().getUkList()));
            skill.setTop10Tier(calculateSkill(90, entry.getValue().getUkList()));
            skill.setUser(100.0*entry.getValue().getAverageMastery());

            data.setSkillData(skill);

            data.setType(entry.getValue().getType());

            data.setSequence(entry.getValue().getSequence());


            List<UKDetailDTO> uklist = new ArrayList<UKDetailDTO>();
            for(Map.Entry<Integer, String> ukentry : entry.getValue().getUkList().entrySet()){
                UKDetailDTO ukdetail = new UKDetailDTO();
                ukdetail.setId(ukentry.getKey().toString());
                ukdetail.setName(ukentry.getValue());
                ukdetail.setSkillScore(ukSkillMap.get(ukentry.getKey()));

                uklist.add(ukdetail);
            }
            data.setUkDetailList(uklist);


            outputList.add(data);
        }

        return outputList;
    }

    //임시 스킬 계산기 (50,90 반음)
    private double calculateSkill(int i, Map<Integer,String> ukMap) {
        //Read top10, top50
        Path path = null;
        String filepathSuffix = "statistics/uk_" + i + "_percentile.json";
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

        int count = 0;
        double total = 0;
        for(Map.Entry<Integer, String> ukentry : ukMap.entrySet()){
            //Get key as string
            String ukIDStr = ukentry.getKey().toString();

            //Get mastery from jsonobject => add to total
            if(result.has(ukIDStr)){
                count++;
                total += result.get(ukIDStr).getAsFloat();
            }
        }
        
        if(count == 0)
            return 0;

        return 100 * total / (double)count;
    }

    @Override
    public List<ChapterDetailDTO> getChapterListOfUserInRange(String userID, String range, String subrange) {
        // TODO Auto-generated method stub
        return null;
    }
}

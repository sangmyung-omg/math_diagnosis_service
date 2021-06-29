package com.tmax.WaplMath.AnalysisReport.service.chapter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SkillStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UKDetailDTO;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.InvalidArgumentException;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

/**
 * Chapter service v2 interface
 * @author Jonghyun Seong
 */
@Service("ChapterServiceV1")
public class ChapterServiceV1 implements ChapterServiceBase{
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    CurriculumInfoRepo currInfoRepo;

    @Autowired
    @Qualifier("AR-UserInfoRepo")
    UserInfoRepo userInfoRepo;

    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    UserKnowledgeRepo ukInfoRepo;


    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUser(String userID) {
        List<Curriculum> list = currInfoRepo.getAllCurriculumOfUser(userID);

        List<ChapterDetailDTO> outList = new ArrayList<>();
        list.forEach(curr->outList.add(getChapterDetailFromCurriculum(userID, curr)));

        return outList;
    }

    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUserChapterOnly(String userID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUserSectionOnly(String userID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUserSubSectionOnly(String userID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ChapterDetailDTO> getChapterListOfUserInRange(String userID, String range, String subrange) {
        //Exception handling for input parameters
        if(userID == null){
            throw new InvalidArgumentException();
        }

        //Get user info from Repo
        User userInfo = userInfoRepo.getUserInfoByUUID(userID);

        if(userInfo == null) {
            throw new GenericInternalException("ERR-0005", "Can not find valid user Info");
        }
    

        //Case 1: range = year : fetch year specific chapters
        if(range != null && range.equals("year")){
            return getChaptersGradeRange(userID, subrange);
        }
        
        //Case 2: range = recent : fetch recent chapters
        if(range != null && range.equals("recent")){
            return getChaptersRecent(userID, subrange);
        }


        //Case default: get all chapters
        if(range != null && !range.equals("all")){
            throw new InvalidArgumentException("Unsupported range value");
        }
        
        
        return getAllChapterListOfUser(userID);
    }

    private List<ChapterDetailDTO> getChaptersGradeRange(String userID, String subrange){
        //range argument exception
        if(subrange == null){
            throw new InvalidArgumentException("subrange is missing");
        }

        //ADD: 2021-06-29 jonghyun seong --> add option to bound range.
        List<Curriculum> currList = null;

        List<String> subrangeList = Arrays.asList(subrange.split("\\*"));

        if(subrangeList.size() == 0)
            throw new GenericInternalException(ARErrorCode.GENERIC_ERROR,"Invalid subrange format");

        
        String rangeParam = subrangeList.size() == 2 ? subrangeList.get(1) : "";

        switch(rangeParam){
            case "seconly":
                currList =  currInfoRepo.getSectionsLikeId(subrangeList.get(0));
                break;
            case "subseconly":
                currList =  currInfoRepo.getSubSectionLikeId(subrangeList.get(0));
                break;
            case "chaponly":
                currList =  currInfoRepo.getChaptersLikeId(subrangeList.get(0));
                break;
            case "partonly":
                currList = currInfoRepo.getPartsLikeId(subrangeList.get(0));
                break;
            case "partinc":
                currList = currInfoRepo.getPartsNotNullLikeId(subrangeList.get(0));
                break;
            case "":
            default:
                currList =  currInfoRepo.getChaptersLikeId(subrangeList.get(0));    
        }
        
        List<ChapterDetailDTO> output = new ArrayList<>();

        //Fill the output list from the output
        currList.forEach( curr -> output.add(getChapterDetailFromCurriculum(userID, curr)));


        return output;
    }


    private List<ChapterDetailDTO> getChaptersRecent(String userID, String subrange){
        //INFO: temp --> recent info needs LRS. thus now, it will return the last semesters results

        int listSize = 5;
        try {
            if(subrange != null)
                listSize = Integer.parseInt(subrange);
        }
        catch (Throwable e){
            throw new InvalidArgumentException("Invalid subrange value");
        }

        //Get UK Data from user
        List<UserKnowledge> knowList = ukInfoRepo.getUserKnowledge(userID);
        Map<String, Long> currIDUpdateMap = new HashMap<>();
        knowList.forEach(know -> {
            //Cut the string to merge into chapter level
            String currID = know.getUk().getCurriculumId().substring(0, 11);

            //If currID does not have the currID yet, then push
            if(!currIDUpdateMap.containsKey(currID)){
                currIDUpdateMap.put(currID, know.getUpdateDate().getTime());
                return;
            }

            //If existing value, then put the max time val
            currIDUpdateMap.put(currID, Math.max(currIDUpdateMap.get(currID), know.getUpdateDate().getTime()));
        });

        //Sort the map FROM: https://howtodoinjava.com/java/sort/java-sort-map-by-values/
        Map<String, Long> sortedMap = new LinkedHashMap<>();
        currIDUpdateMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> sortedMap.put(e.getKey(), e.getValue()));

        // System.out.println(sortedMap);

        List<Curriculum> currList = currInfoRepo.getFromCurrIdList(new ArrayList<String>(sortedMap.keySet()).subList(0, Math.min(listSize, sortedMap.keySet().size()) ));
        List<ChapterDetailDTO> output = new ArrayList<>();

        currList.forEach( curr -> output.add(getChapterDetailFromCurriculum(userID, curr)));

        return output;
    }

    private ChapterDetailDTO getChapterDetailFromCurriculum(String userID, Curriculum curr){
        ChapterDetailDTO chapDetail = new ChapterDetailDTO();

        chapDetail.setId(curr.getCurriculumId());
        chapDetail.setImagePath("/dummy.png");
        chapDetail.setName(curr.getChapter());
        chapDetail.setSequence(curr.getCurriculumSequence());

        chapDetail.setType(getTypeFromChapter(curr));

        Map<Integer, UserKnowledge> ukMap = getChapterUKData(userID, curr.getCurriculumId());


        //
        chapDetail.setSkillData(getSkillStatFromUKMap(ukMap));

        chapDetail.setUkDetailList(getUKDetailFromUKMap(ukMap));

        return chapDetail;
    }

    private String getTypeFromChapter(Curriculum curr) {
        String type = "소단원";

        if(curr.getSubSection() == null)
            type = "중단원";
        
        if(curr.getSection() == null)
            type = "대단원";

        if(curr.getChapter() == null)
            type = "파트";

        // logger.info(curr.getSubSection()  + "-"+ curr.toString()  + "-"+ curr.getSection()  + "-"+ curr.getChapter() + "-"+ type);

        return type;
    }



    private Map<Integer, UserKnowledge> getChapterUKData(String userID, String currID){
        List<UserKnowledge> ukList = ukInfoRepo.getKnowledgeOfCurrLike(userID, currID);

        //Make a map to merge duplicate uks
        Map<Integer, UserKnowledge> ukMap = new HashMap<>();
        ukList.forEach(uk -> ukMap.put(uk.getUkId(), uk));

        return ukMap;
    }

    private SkillStatDTO getSkillStatFromUKMap(Map<Integer, UserKnowledge> ukMap){
        SkillStatDTO output = new SkillStatDTO();

        //Calc user average
        double score = 0.0;
        int count = 0;
        for(Map.Entry<Integer,UserKnowledge> entry : ukMap.entrySet()){
            score += (double)entry.getValue().getUkMastery();
            count++;
        }

        //Leave NaN to show that error has occured
        output.setUser(100 * score / count);

        output.setTop10Tier(calculateSkill(90, ukMap));
        output.setAverage(calculateSkill(50, ukMap));


        return output;
    }

    private List<UKDetailDTO> getUKDetailFromUKMap(Map<Integer, UserKnowledge> ukMap){
        List<UKDetailDTO> outList = new ArrayList<>();

        for(Map.Entry<Integer, UserKnowledge> entry : ukMap.entrySet()){
            UKDetailDTO ukdetail = new UKDetailDTO();

            ukdetail.setId(entry.getValue().getUkId().toString());
            ukdetail.setName(entry.getValue().getUk().getUkName());
            ukdetail.setSkillScore(entry.getValue().getUkMastery());

            outList.add(ukdetail);
        }

        return outList;
    }


    //임시 스킬 계산기 (50,90 반음)
    private double calculateSkill(int i, Map<Integer,UserKnowledge> ukMap) {
        //Read top10, top50
        Path path = null;
        try {
            path = ResourceUtils.getFile("classpath:uk_" + i + "_percentile.json").toPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileReader reader = null;
        try {
            reader = new FileReader(path.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonObject result = (JsonObject)JsonParser.parseReader(reader);

        int count = 0;
        double total = 0;
        for(Map.Entry<Integer, UserKnowledge> ukentry : ukMap.entrySet()){
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
}

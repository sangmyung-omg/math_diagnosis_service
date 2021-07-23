package com.tmax.WaplMath.AnalysisReport.service.chapter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SkillStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UKDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreServiceBaseV0;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.InvalidArgumentException;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.repository.uk.UkRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Chapter service v2 interface
 * @author Jonghyun Seong
 */
@Slf4j
@Service("ChapterServiceV1")
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(value="file:${external-config.url}/application.properties", ignoreResourceNotFound=true),
})
public class ChapterServiceV1 implements ChapterServiceBase{
    @Value("${external-config.url}")
    private String externalConfigURL;
    
    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    private CurriculumInfoRepo currInfoRepo;

    @Autowired
    @Qualifier("AR-UserInfoRepo")
    private UserInfoRepo userInfoRepo;

    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    private UserKnowledgeRepo ukInfoRepo;

    @Autowired
    private UkRepo ukRepo;

    @Autowired
    @Qualifier("UserStatisticsServiceV0")
    private UserStatisticsServiceBase userStatSvc;

    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Autowired
    @Qualifier("CurrStatisticsServiceV0")
    private CurrStatisticsServiceBase currStatSvc;

    @Autowired
    @Qualifier("AR-WaplScoreServiceV0")
    private WaplScoreServiceBaseV0 waplScoreSvc;


    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUser(String userID) {
        return getAllChapterListOfUser(userID, false);
    }

    public List<ChapterDetailDTO> getAllChapterListOfUser(String userID, boolean isLightMode) {
        List<Curriculum> list = currInfoRepo.getAllCurriculumOfUser(userID);

        List<ChapterDetailDTO> outList = new ArrayList<>();

        //Get curr mastery map first get call
        Map<String, Float> currMasteryMap = getUserCurriculumMasteryMap(userID);
        list.forEach(curr->outList.add(getChapterDetailFromCurriculum(userID, curr,currMasteryMap, isLightMode)));

        return outList;
    }

    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUserChapterOnly(String userID) {
        return getChapterListOfUserInRange(userID, "year","중*chaponly");
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
        // //Exception handling for input parameters
        // if(userID == null){
        //     throw new InvalidArgumentException();
        // }

        // //Get user info from Repo
        // User userInfo = userInfoRepo.getUserInfoByUUID(userID);

        // if(userInfo == null) {
        //     log.error("Invalid User data." + userID);
        //     throw new GenericInternalException("ERR-0005", "Can not find valid user Info." + userID);
        // }
    

        // //Case 1: range = year : fetch year specific chapters
        // if(range != null && range.equals("year")){
        //     return getChaptersGradeRange(userID, subrange, false);
        // }
        
        // //Case 2: range = recent : fetch recent chapters
        // if(range != null && range.equals("recent")){
        //     return getChaptersRecent(userID, subrange, false);
        // }


        // //Case default: get all chapters
        // if(range != null && !range.equals("all")){
        //     throw new InvalidArgumentException("Unsupported range value");
        // }
        
        
        // return getAllChapterListOfUser(userID);
        return getChapterListOfUserInRange(userID, range, subrange, false);
    }

    //TODO -- refactor this method
    public List<ChapterDetailDTO> getChapterListOfUserInRange(String userID, String range, String subrange, boolean isLightMode) {
        //Exception handling for input parameters
        if(userID == null){
            throw new InvalidArgumentException();
        }

        //Get user info from Repo
        User userInfo = userInfoRepo.getUserInfoByUUID(userID);

        if(userInfo == null) {
            log.error("Invalid User data." + userID);
            throw new GenericInternalException("ERR-0005", "Can not find valid user Info." + userID);
        }
    

        //Case 1: range = year : fetch year specific chapters
        if(range != null && range.equals("year")){
            return getChaptersGradeRange(userID, subrange, isLightMode);
        }
        
        //Case 2: range = recent : fetch recent chapters
        if(range != null && range.equals("recent")){
            return getChaptersRecent(userID, subrange, isLightMode);
        }


        //Case default: get all chapters
        if(range != null && !range.equals("all")){
            throw new InvalidArgumentException("Unsupported range value");
        }
        
        
        return getAllChapterListOfUser(userID, isLightMode);
    }

    private List<ChapterDetailDTO> getChaptersGradeRange(String userID, String subrange, boolean isLightMode){
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
        //Get curr mastery map first get call
        Map<String, Float> currMasteryMap = getUserCurriculumMasteryMap(userID);
        currList.forEach( curr -> output.add(getChapterDetailFromCurriculum(userID, curr, currMasteryMap, isLightMode)));


        return output;
    }


    private List<ChapterDetailDTO> getChaptersRecent(String userID, String subrange, boolean isLightMode){
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


        //Get curr mastery map first get call
        Map<String, Float> currMasteryMap = getUserCurriculumMasteryMap(userID);
        currList.forEach( curr -> output.add(getChapterDetailFromCurriculum(userID, curr, currMasteryMap, isLightMode)));

        return output;
    }

    private ChapterDetailDTO getChapterDetailFromCurriculum(String userID, Curriculum curr, Map<String, Float> currMasteryMap, boolean isLightMode){
        ChapterDetailDTO chapDetail = new ChapterDetailDTO();

        chapDetail.setId(curr.getCurriculumId());
        chapDetail.setName(curr.getChapter());
        chapDetail.setSequence(curr.getCurriculumSequence());

        chapDetail.setType(getTypeFromChapter(curr));

        if(!isLightMode){
            Map<Integer, UserKnowledge> ukMap = getChapterUKData(userID, curr.getCurriculumId());
            chapDetail.setImagePath("/dummy.png");
            chapDetail.setUkDetailList(getUKDetailFromUKMap(ukMap));
            
        } 
        chapDetail.setSkillData(getSkillFromCurriculumMap(userID, curr.getCurriculumId(), currMasteryMap));           

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


        //TODO: Find out why UserKnowledge.uk is giving null
        //Collect all uk to Set
        Set<Integer> ukIDList = ukList.stream().map(uknow -> uknow.getUkId()).collect(Collectors.toSet());

        //Get the UK infos
        Iterable<Uk> ukDataList = ukRepo.findAllById(ukIDList);

        //Set UK to outputmap
        ukDataList.forEach(uk -> ukMap.get(uk.getUkId()).setUk(uk));

        return ukMap;
    }

    @Deprecated
    SkillStatDTO getSkillStatFromUKMap(Map<Integer, UserKnowledge> ukMap){
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

    private SkillStatDTO getSkillFromCurriculumMap(String userID, String currID, Map<String,Float> currMasteryMap){
        //Get user's current score of curriculum
        double userscore = currMasteryMap.get(currID);

        //Get user's percentile
        List<Float> sortedMastery = currStatSvc.getStatistics(currID, CurrStatisticsServiceBase.STAT_MASTERY_SORTED).getAsFloatList();
        double userpercentile = 100*ukStatSvc.getPercentile((float)userscore, sortedMastery);


        //Get wapl score of currID
        WAPLScoreDTO waplScoreDto = waplScoreSvc.getCurriculumWaplScore(userID, currID);

        //Get mean
        double average = 100*currStatSvc.getStatistics(currID, CurrStatisticsServiceBase.STAT_MASTERY_MEAN).getAsFloat();

        //Get STD
        double globalstd = 100*currStatSvc.getStatistics(currID, CurrStatisticsServiceBase.STAT_MASTERY_STD).getAsFloat();

        //Get top 10
        double top10Tier = 100*sortedMastery.get((int)Math.round(0.9 * sortedMastery.size()));

        return SkillStatDTO.builder()
                           .user(100*userscore)
                           .userpercentile(userpercentile)
                           .waplscore(100*waplScoreDto.getScore())
                           .waplscorepercentile(100*waplScoreDto.getPercentile())
                           .average(average)
                           .top10Tier(top10Tier)
                           .globalstd(globalstd)
                           .build();
    }

    private Map<String, Float> getUserCurriculumMasteryMap(String userID){
        //Get user curriculum Mastery map.
        Statistics stat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_CURRICULUM_MASTERY_MAP);

        //If stat is null, throw error. invoke the generation flow?
        if(stat == null){
            throw new GenericInternalException(ARErrorCode.INVALID_MASTERY_DATA, String.format("Curriculum mastery data for user [%s] does not exist. If this error persists, try calling mastery update first.",userID));
        }

        //Convert to Map from json data
        Type type = new TypeToken<Map<String, Float>>(){}.getType();
        return new Gson().fromJson(stat.getData(), type);
    }

    private List<UKDetailDTO> getUKDetailFromUKMap(Map<Integer, UserKnowledge> ukMap){
        List<UKDetailDTO> outList = new ArrayList<>();

        for(Map.Entry<Integer, UserKnowledge> entry : ukMap.entrySet()){
            outList.add(UKDetailDTO.builder()
                                   .id(entry.getKey().toString())
                                   .name(entry.getValue().getUk().getUkName())
                                   .skillScore(entry.getValue().getUkMastery())
                                   .build());
        }

        return outList;
    }


    //임시 스킬 계산기 (50,90 반음)
    private double calculateSkill(int i, Map<Integer,UserKnowledge> ukMap) {
        //Read top10, top50
        Path path = null;
        String filepathSuffix = "statistics/uk_" + i + "_percentile.json";
        try {path = ResourceUtils.getFile("classpath:" + filepathSuffix).toPath();}
        catch (FileNotFoundException e) {log.debug("File not found internally: "+ filepathSuffix);}

        if(path == null){ //external file read
            try {path = ResourceUtils.getFile("file:" + externalConfigURL + "/" + filepathSuffix).toPath();} 
            catch (FileNotFoundException e) {log.error("File also not found externally.: "+ filepathSuffix);}
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

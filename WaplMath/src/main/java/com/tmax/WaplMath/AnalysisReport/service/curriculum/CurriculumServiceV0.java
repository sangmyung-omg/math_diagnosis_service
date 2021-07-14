package com.tmax.WaplMath.AnalysisReport.service.curriculum;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumDataDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumSimpleDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeDetailDTO;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.userknowledge.UserKnowledgeServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.uk.Uk;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.UkRepository;
import com.tmax.WaplMath.Recommend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("AR-CurriculumServiceV0")
public class CurriculumServiceV0 implements CurriculumServiceBase {
    @Autowired
    UserRepository userRepo;

    @Autowired
    CurriculumRepository currRepo;

    @Autowired
    CurriculumInfoRepo currInfoRepo;

    @Autowired
    UserStatisticsServiceBase userStatSvc;

    @Autowired
    UKStatisticsServiceBase ukStatSvc;

    @Autowired
    CurrStatisticsServiceBase currStatSvc;

    @Autowired
    UkRepository ukRepo;

    @Autowired
    UserKnowledgeServiceBase userKnowledgeSvc;

    @Override
    public CurriculumDataDTO getByIdList(String userID, List<String> currIDList, Set<String> excludeSet) {
        //Get the curriculum Info
        List<Curriculum> currList = (List<Curriculum>)currRepo.findAllById(currIDList);

        //Get userInfo to get Grade
        Optional<User> user = userRepo.findById(userID);
        if(!user.isPresent()){
            log.warn("No user Information found for " + userID);
        }

        return buildFromCurriculum(userID, user.get().getGrade(), currList, excludeSet);
    }

    @Override
    public CurriculumDataDTO searchWithConditions(String userID, String searchTerm, String typeRange, String mode, String range,
                                                        boolean subSearch, String order, Set<String> excludeSet) {
        //Get all by search terms
        List<Curriculum> searchResult;
        if(subSearch){
            searchResult = currInfoRepo.getCurriculumLikeId(searchTerm);
        }
        else {
            searchResult = currInfoRepo.getFromCurrIdList(Arrays.asList(searchTerm));
        }

        //Filter by type range
        if(!typeRange.isEmpty()){
            Set<String> typeSet = Arrays.asList(typeRange.split(",")).stream().collect(Collectors.toSet());
            searchResult = searchResult.stream()
                                        .filter(curr -> checkTypeRange(curr, typeSet))
                                        .collect(Collectors.toList());
        }

        //Order by


        //Build output List
        //Get userInfo to get Grade
        Optional<User> user = userRepo.findById(userID);
        if(!user.isPresent()){
            log.warn("No user Information found for " + userID);
        }

        return buildFromCurriculum(userID, user.get().getGrade(), searchResult, excludeSet);
    }

    private boolean checkTypeRange(Curriculum curr, Set<String> typeSet){
        return typeSet.contains(getCurriculumType(curr));
    }

    //TODO. make this prune to currID stype change
    public String getCurriculumType(Curriculum curr){
        String curriculumId = curr.getCurriculumId();
        String type = "unknown";
        
        switch(curriculumId.length()){
            case 17:
                type = "subsection";
                break;
            case 14:
                type = "section";
                break;
            case 11:
                type = "chapter";
                break;
            default:

        }

        return type;
    }

    public String getName(Curriculum curr, String type){
        String name;
        switch(type){
            case "chapter":
                name = curr.getChapter();
                break;
            case "section":
                name = curr.getSection();
                break;
            case "subsection":
                name = curr.getSubSection();
                break;
            default:
                name = null;
        } 

        return name;
    }


    private CurriculumDataDTO buildFromCurriculum(String userID, String grade, List<Curriculum> currList, Set<String> excludeSet){
        //If excludeSet includes currDataList, this method is useless (ukList is dependent on ukIdSet)
        if(excludeSet.contains("currDataList")){
            return new CurriculumDataDTO();
        }

        //Total UKSet
        Set<Integer> ukIdSetTotal = new HashSet<>();

        List<CurriculumDataDetailDTO> dataList = new ArrayList<>();
        for(Curriculum curr : currList){
            //Get percentile  LUT
            List<Float> scoreLUT = getUserPercentileLUT(userID, grade, curr.getCurriculumId());
            
            //Get related UK List
            List<Uk> ukList = ukRepo.findByLikelyCurriculumId(curr.getCurriculumId());
            Set<Integer> ukIdSet = ukList.stream().map(uk->uk.getUkId()).collect(Collectors.toSet());

            dataList.add(CurriculumDataDetailDTO.builder()
                                                .basic(buildBasicInfo(curr))
                                                .score(getUserScore(userID, grade, curr.getCurriculumId(), scoreLUT))
                                                .waplscore(getWaplScore(userID, grade, curr.getCurriculumId(), scoreLUT, ukIdSet))
                                                .stats(getStats(curr.getCurriculumId()))
                                                .ukIdList(new ArrayList<>(ukIdSet))
                                                .build()
            );

            ukIdSetTotal.addAll(ukIdSet);
        }
        //Get ukList of curriculum

        return CurriculumDataDTO.builder()
                                .currDataList(dataList)
                                .ukKnowledgeList(!excludeSet.contains("ukKnowledgeList") ? buildUKKnowledgeList(userID, ukIdSetTotal) : null)
                                .build();
    }

    private CurriculumSimpleDTO buildBasicInfo(Curriculum curr) {
        //Determine type        
        String type = null;
        String name = null;

        // //Eliminatory type determination
        // if(curr.getSubSection() == null){
        //     type = "section";
        //     name = curr.getSection();
        // }
        // else if(curr.getSection() == null){
        //     type = "chapter";
        //     name = curr.getChapter();
        // }
        // else if(curr.getChapter() == null){
        //     type = "part";
        //     name = curr.getPart();
        // } 
        // else {
        //     type = "subsection";
        //     name = curr.getSubSection();
        // }

        //Determine by length
        switch(curr.getCurriculumId().length()){
            case 17:
                type = "subsection";
                name = curr.getSubSection();
                break;
            case 14:
                type = "section";
                name = curr.getSection();
                break;
            case 11:
                type = "chapter";
                name = curr.getChapter();
                break;
            default:

        }

        return CurriculumSimpleDTO.builder()
                                  .name(name)
                                  .id(curr.getCurriculumId())
                                  .seq(curr.getCurriculumSequence())
                                  .type(type)
                                  .build();
    }
    
    private List<Float> getUserPercentileLUT(String userID, String grade, String currID){
        //Percentile calc
        Statistics perLUT = currStatSvc.getStatistics(currID, 
                                                       CurrStatisticsServiceBase.STAT_MASTERY_PERCENTILE_LUT + "_grade_" + grade);
        if(perLUT == null){
            log.warn("STAT_MASTERY_PERCENTILE_LUT does not exist for curriculum " + currID);
            return null;
        }

        List<Float> scoreLUT = perLUT.getAsFloatList();

        return scoreLUT;
    }

    private PersonalScoreDTO getUserScore(String userID, String grade, String currID, List<Float> scoreLUT){
        //Get score from stat table
        //TODO: is there any way to cache this map? REDIS maybe?
        Statistics currMasteryStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_CURRICULUM_MASTERY_MAP);

        if(currMasteryStat == null){
            log.warn("Curriculum Mastery map does not exist for user " + userID);
            return null;
        }

        //Make it to map
        Type type = new TypeToken<Map<String, Float>>(){}.getType();
        Map<String, Float> masteryMap = new Gson().fromJson(currMasteryStat.getData(), type);

        Float score = masteryMap.get(currID);
        Float percentile = ukStatSvc.getPercentile(score, scoreLUT);

        return PersonalScoreDTO.builder().score(score).percentile(percentile).build();
    }

    private PersonalScoreDTO getWaplScore(String userID, String grade, String currID, List<Float> scoreLUT, Set<Integer> ukIDList){
        //Get score from stat table
        //TODO: is there any way to cache this map? REDIS maybe?
        Statistics waplMasteryStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_WAPL_SCORE_MASTERY);

        if(waplMasteryStat == null){
            log.warn("Curriculum Mastery map does not exist for user " + userID);
            return null;
        }

        //Make it to map
        Type type = new TypeToken<List<Map<String, Float>>>(){}.getType();
        List<Map<String, Float>> masteryMapList = new Gson().fromJson(waplMasteryStat.getData(), type);

        //For all ukIDList add score
        int count = 0;
        float total = 0.0f;
        Map<String, Float> map = masteryMapList.get(0);
        for(Integer ukID : ukIDList){
            if(map.containsKey(ukID.toString())){
                total += map.get(ukID.toString());
                count++;
            }
        }
        Float score = total/count;
        Float percentile = ukStatSvc.getPercentile(score, scoreLUT);

        return PersonalScoreDTO.builder().score(score).percentile(percentile).build();
    }

    private GlobalStatisticDTO getStats(String currID){
        //Mean
        Statistics meanStat = currStatSvc.getStatistics(currID, CurrStatisticsServiceBase.STAT_MASTERY_MEAN);
        Float mean = null;
        if(meanStat != null){
            mean = 100 * meanStat.getAsFloat();
        }

        //Median. TODO not supported yet
        Statistics medianStat = currStatSvc.getStatistics(currID, CurrStatisticsServiceBase.STAT_MASTERY_MEDIAN);
        Float median = null;
        if(medianStat != null){
            median = 100 * medianStat.getAsFloat();
        }

        //std.
        Statistics stdStat = currStatSvc.getStatistics(currID, CurrStatisticsServiceBase.STAT_MASTERY_STD);
        Float std = null;
        if(stdStat != null){
            std = 100 * stdStat.getAsFloat();
        }

        //Histogram + Total cnt
        Statistics sortedListStat = currStatSvc.getStatistics(currID, CurrStatisticsServiceBase.STAT_MASTERY_PERCENTILE_LUT);
        List<Integer> histogram = null;
        Integer totalCnt = 0;
        int histogramSize = 10;
        if(sortedListStat != null){
            //Create slots. TODO --> make histogram size as option
            histogram = new ArrayList<>(java.util.Collections.nCopies(histogramSize, 0));
            float step = 1.0f / (float)histogramSize;
            for(Float mastery : sortedListStat.getAsFloatList()){
                int idx = Math.min((int) Math.floor(mastery / step), histogramSize - 1);
                histogram.set(idx, histogram.get(idx) + 1);
            }

            totalCnt = sortedListStat.getAsFloatList().size();
        }


        return GlobalStatisticDTO.builder()
                                 .mean(mean)
                                 .median(median)
                                 .std(std)
                                 .histogram(histogram)
                                 .totalCnt(totalCnt)
                                 .build();
    }

    private List<UkUserKnowledgeDetailDTO> buildUKKnowledgeList(String userID, Set<Integer> ukIdSet){
        return userKnowledgeSvc.getByUkIdList(userID, new ArrayList<>(ukIdSet));
    }
}

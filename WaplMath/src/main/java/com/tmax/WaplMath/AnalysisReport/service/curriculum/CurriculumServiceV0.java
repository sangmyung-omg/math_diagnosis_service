package com.tmax.WaplMath.AnalysisReport.service.curriculum;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import com.tmax.WaplMath.AnalysisReport.util.statistics.StatisticsUtil;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.UserNotFoundException;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.repository.curriculum.CurriculumRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import com.tmax.WaplMath.Recommend.repository.UkRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("AR-CurriculumServiceV0")
public class CurriculumServiceV0 implements CurriculumServiceBase {
    @Autowired
    UserRepo userRepo;

    @Autowired
    CurriculumRepo currRepo;

    @Autowired
    CurriculumInfoRepo currInfoRepo;

    @Autowired
    UserStatisticsServiceBase userStatSvc;

    @Autowired
    UKStatisticsServiceBase ukStatSvc;

    @Autowired
    CurrStatisticsServiceBase currStatSvc;

    @Autowired
    @Qualifier("RE-UkRepo")
    UkRepo ukRepo;

    @Autowired
    UserKnowledgeServiceBase userKnowledgeSvc;

    @Override
    public CurriculumDataDTO getByIdList(String userID, List<String> currIDList, Set<String> excludeSet) {
        //Exception handling for currID's size is 0
        if(currIDList.size() == 0){ return getEmptyCurriculumData();}

        //Get the curriculum Info.
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
        // List<Curriculum> searchResult = new ArrayList<>();

        //Split searchTerms to list by comma
        // Set<String> searchSet = Arrays.asList(searchTerm.split(",")).stream().collect(Collectors.toSet());
        // for(String searchStr : searchSet){
        //     if(subSearch){
        //         searchResult.addAll( currInfoRepo.getCurriculumLikeId(searchStr) );
        //     }
        //     else {
        //         searchResult.addAll( currInfoRepo.getFromCurrIdList(Arrays.asList(searchStr)) );
        //     }
        // }

        //Parallel opt
        Set<String> searchSet = Arrays.asList(searchTerm.split(",")).stream().collect(Collectors.toSet());
        List<Curriculum> searchResult = searchSet.stream().parallel().flatMap(searchStr -> {
            if(subSearch){
                return currInfoRepo.getCurriculumLikeId(searchStr).stream();
            }
            else {
                return currInfoRepo.getFromCurrIdList(Arrays.asList(searchStr)).stream();
            }
        }).collect(Collectors.toList());

        //Filter by type range
        if(!typeRange.isEmpty()){
            Set<String> typeSet = Arrays.asList(typeRange.split(",")).stream().collect(Collectors.toSet());
            searchResult = filterTypeCurriculumList(searchResult, typeSet);
        }

        //Order List by condition


        //Build output List
        return buildFromCurriculum(userID, getUserInfo(userID).getGrade(), searchResult, excludeSet);
    }

    @Override
    public CurriculumDataDTO searchByYear(String userID, String schoolType, String year, String typeRange, String order, Set<String> excludeSet) {
        //Build searchTerm from schoolType
        String searchTerm = "";
        switch(schoolType){
            case "prim":
                searchTerm += "초등-초";
                break;
            case "mid":
                searchTerm += "중등-중";
                break;
            case "high":
                searchTerm += "고등-고";
                break;
            default:
                throw new GenericInternalException(ARErrorCode.INVALID_PARAMETER, "Unsupported school type. " + schoolType);
        }

        return searchWithConditions(userID, searchTerm + year, typeRange, null, null, true, order, excludeSet);
    }

    @Override
    public CurriculumDataDTO searchRecent(String userID, Integer count, String castTo, String order, Set<String> excludeSet) {
        return searchRecent(userID, count, castTo, order, "", excludeSet);
    }

    @Override
    public CurriculumDataDTO searchRecent(String userID, Integer count, String castTo, String order, String idFilter, Set<String> excludeSet) {
        //Get recent Curr List from stat table
        Statistics recentCurrStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_RECENT_CURR_ID_LIST);
        if(recentCurrStat == null){
            log.error("[{}] Recent curriculum list not found in stat table. Please check if LRS info.", userID);
            throw new GenericInternalException(ARErrorCode.INVALID_STATISTICS_ERROR, 
                                               "Recent curriculum list not found in stat table. Please check if LRS info is properly submitted.");
        }

        List<String> recentCurrIDList = recentCurrStat.getAsStringList();

        //Filter the currIDList
        if(!idFilter.isEmpty()){
            recentCurrIDList = new ArrayList<>(recentCurrIDList.stream()
                                                               .filter(id -> id.startsWith(idFilter))
                                                               .collect(Collectors.toSet()) 
                                                               );
        }

        //Cut resultIDList by count
        // recentCurrIDList = recentCurrIDList.subList(0, Math.min(Math.max(0, count), recentCurrIDList.size()));

        //Fill currList depending on typeRange
        //split typerange to Set
        Set<String> castSet = new HashSet<>();
        if(castTo != null && !castTo.isEmpty())
            castSet.addAll( Arrays.asList(castTo.split(",")).stream().collect(Collectors.toSet()) );

        List<String> searchCurrIDList = recentCurrIDList.stream()
                                                        .flatMap(id -> {
                                                            //None castset case
                                                            if(castSet.isEmpty()){
                                                                return Arrays.asList(id).stream();
                                                            }

                                                            Set<String> currIDSet = new HashSet<>();
                                                            int length = id.length();

                                                            //Cast set cases
                                                            if(castSet.contains("chapter") && length >= 11)
                                                                currIDSet.add(castCurriculumID(id, "chapter"));

                                                            if(castSet.contains("section") && length >= 14)
                                                                currIDSet.add(castCurriculumID(id, "section"));

                                                            if(castSet.contains("subsection") && length >= 17)
                                                                currIDSet.add(castCurriculumID(id, "subsection"));

                                                            return currIDSet.stream();
                                                        })
                                                        .collect(Collectors.toList());


        //cut list to given count --> then collect List
        List<Curriculum> recentCurrList = searchCurrIDList.subList(0, Math.min(Math.max(0, count), searchCurrIDList.size()))
                                                          .stream()
                                                          .parallel()
                                                          .flatMap(id -> {
                                                            if(castSet.isEmpty()){
                                                                Optional<Curriculum> curr = currInfoRepo.findById(id);
                                                                if(curr.isPresent())
                                                                    return Arrays.asList(curr.get()).stream();
                                                            }

                                                            //Select currID ==> the highest of the castTo
                                                            String currIDsel = getHighestCurrID(id, castSet);
                                                            if(castSet.contains("chapter"))
                                                                return currInfoRepo.getChaptersLikeId(currIDsel).stream();
                                                            
                                                            if(castSet.contains("section"))
                                                                return currInfoRepo.getSectionsLikeId(currIDsel).stream();
                                                            
                                                            if(castSet.contains("subsection"))
                                                                return currInfoRepo.getSubSectionLikeId(currIDsel).stream();
                                                        
                                                            return new ArrayList<Curriculum>().stream();
                                                          })
                                                          .collect(Collectors.toList());
                                                          

        //Parallel optimization
        // List<Curriculum> recentCurrList = recentCurrIDList.stream()
        //                                                   .parallel()
        //                                                   .flatMap(currID -> {
        //     if(castSet.isEmpty()){
        //         Optional<Curriculum> curr = currInfoRepo.findById(currID);
        //         if(curr.isPresent())
        //             return Arrays.asList(curr.get()).stream();
        //     }

        //     //Select currID ==> the highest of the castTo
        //     String currIDsel = getHighestCurrID(currID, castSet);
        //     if(castSet.contains("chapter"))
        //         return currInfoRepo.getChaptersLikeId(currIDsel).stream();
            
        //     if(castSet.contains("section"))
        //         return currInfoRepo.getSectionsLikeId(currIDsel).stream();
            
        //     if(castSet.contains("subsection"))
        //         return currInfoRepo.getSubSectionLikeId(currIDsel).stream();
        
        //     return new ArrayList<Curriculum>().stream();
        // }).collect(Collectors.toList());
        


        //Return list
        return buildFromCurriculum(userID, getUserInfo(userID).getGrade(), recentCurrList, excludeSet);
    }

    public String castCurriculumID(String inputID, String type){
        Integer orilen = inputID.length();
        if(type.equals("chapter"))
            return inputID.substring(0, Math.min(orilen,11));
        
        if(type.equals("section"))
            return inputID.substring(0,  Math.min(orilen,14));

        if(type.equals("subsection"))
            return inputID.substring(0,  Math.min(orilen,17));

        return inputID;
    }

    private String getHighestCurrID(String currID, Set<String> castToSet){
        if(castToSet.contains("chapter"))
            return castCurriculumID(currID, "chapter");
        
        if(castToSet.contains("section"))
            return castCurriculumID(currID, "section");

        if(castToSet.contains("subsection"))
            return castCurriculumID(currID, "subsection");

        return currID;
    }

    private User getUserInfo(String userID) {
        //Get userInfo to get Grade
        Optional<User> user = userRepo.findById(userID);
        if(!user.isPresent()){
            log.warn("No user Information found for " + userID);
            throw new UserNotFoundException(userID);
        }
        return user.get();
    }

    private List<Curriculum> filterTypeCurriculumList(List<Curriculum> inputList, Set<String> typeSet){
        return inputList.stream().filter(curr -> checkTypeRange(curr, typeSet)).collect(Collectors.toList());
    }

    private boolean checkTypeRange(Curriculum curr, Set<String> typeSet){
        return typeSet.contains(getCurriculumType(curr));
    }

    private Comparator<CurriculumDataDetailDTO> getCurrDataComparator(String type){
        if(type.equals("mastery")){
            //Return default comparator (sequence compare)
            return new Comparator<CurriculumDataDetailDTO>() {
                @Override
                public int compare(CurriculumDataDetailDTO o1, CurriculumDataDetailDTO o2) {
                    return o1.getScore().getScore().compareTo(o2.getScore().getScore());
                }
            };
        }

        //Return default comparator (sequence compare)
        return new Comparator<CurriculumDataDetailDTO>() {
            @Override
            public int compare(CurriculumDataDetailDTO o1, CurriculumDataDetailDTO o2) {
                return o1.getBasic().getSeq().compareTo(o2.getBasic().getSeq());
            }
        };
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

    private CurriculumDataDTO getEmptyCurriculumData(){
        return new CurriculumDataDTO(new ArrayList<>(), new ArrayList<>());
    }


    private CurriculumDataDTO buildFromCurriculum(String userID, String grade, List<Curriculum> currList, Set<String> excludeSet){
        //If excludeSet includes currDataList, this method is useless (ukList is dependent on ukIdSet)
        if(excludeSet.contains("currDataList")){
            return new CurriculumDataDTO();
        }

        //Total UKSet
        Set<Integer> ukIdSetTotal = new HashSet<>();

        //make Exclude field set for currData (with prefix currData)
        Set<String> cdExcludeSet = excludeSet.stream().filter(data -> data.startsWith("currDataList."))
                                                      .map(data -> data.substring("currDataList.".length()))
                                                      .collect(Collectors.toSet());

        //Build currList
        List<CurriculumDataDetailDTO> dataList = new ArrayList<>();
        for(Curriculum curr : currList){
            //Get percentile  LUT
            List<Float> scoreLUT = getUserPercentileLUT(userID, grade, curr.getCurriculumId());
            
            //Get related UK List
            List<Uk> ukList = ukRepo.findByLikelyCurriculumId(curr.getCurriculumId());
            Set<Integer> ukIdSet = ukList.stream().map(uk->uk.getUkId()).collect(Collectors.toSet());

            dataList.add(CurriculumDataDetailDTO.builder()
                                                .basic(buildBasicInfo(curr))
                                                .score(!cdExcludeSet.contains("score") ? getUserScore(userID, grade, curr.getCurriculumId(), scoreLUT) : null)
                                                .waplscore(!cdExcludeSet.contains("waplscore") ? getWaplScore(userID, grade, curr.getCurriculumId(), scoreLUT, ukIdSet) : null)
                                                .stats(!cdExcludeSet.contains("stats") ? getStats(curr.getCurriculumId()) : null)
                                                .ukIdList(!cdExcludeSet.contains("ukIdList") ? new ArrayList<>(ukIdSet) : null)
                                                .build()
            );

            ukIdSetTotal.addAll(ukIdSet);
        }
        
        //Get ukList of curriculum
        List<UkUserKnowledgeDetailDTO> ukKnowledgeList = null;
        if(!excludeSet.contains("ukKnowledgeList")){
            //Build excludeset
            Set<String> ukExcludeSet = excludeSet.stream().filter(data -> data.startsWith("ukKnowledgeList."))
                                                          .map(data -> data.substring("ukKnowledgeList.".length()))
                                                          .collect(Collectors.toSet());

            ukKnowledgeList = buildUKKnowledgeList(userID, ukIdSetTotal, ukExcludeSet);
        }

        return CurriculumDataDTO.builder()
                                .currDataList(dataList)
                                .ukKnowledgeList(ukKnowledgeList)
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

        return PersonalScoreDTO.builder().score(100 * score).percentile(100 * percentile).build();
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

        //check mastery
        if(masteryMapList.size() == 0){
            log.error("WAPL score mastery is invalid for {}. Type regeneration." , userID);
            return null;
        }

        //For all ukIDList add score
        int count = 0;
        float total = 0.0f;
        Map<String, Float> map = masteryMapList.get(masteryMapList.size() - 1); // get last map
        for(Integer ukID : ukIDList){
            if(map.containsKey(ukID.toString())){
                total += map.get(ukID.toString());
                count++;
            }
        }
        Float score = total/count;
        Float percentile = ukStatSvc.getPercentile(score, scoreLUT);

        return PersonalScoreDTO.builder().score(100 * score).percentile(100 * percentile).build();
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

        //Histogram + Total cnt + percentile
        Statistics sortedListStat = currStatSvc.getStatistics(currID, CurrStatisticsServiceBase.STAT_MASTERY_PERCENTILE_LUT);
        List<Integer> histogram = null;
        List<Float> percentile = null;
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


            //percentile
            percentile = StatisticsUtil.createPercentileLUT(sortedListStat.getAsFloatList(), 101).stream().map(data -> 100*data).collect(Collectors.toList());
        }

        //percentile



        return GlobalStatisticDTO.builder()
                                 .mean(mean)
                                 .median(median)
                                 .std(std)
                                 .histogram(histogram)
                                 .percentile(percentile)
                                 .totalCnt(totalCnt)
                                 .build();
    }

    private List<UkUserKnowledgeDetailDTO> buildUKKnowledgeList(String userID, Set<Integer> ukIdSet, Set<String> excludeSet){
        return userKnowledgeSvc.getByUkIdList(userID, new ArrayList<>(ukIdSet), excludeSet);
    }
}

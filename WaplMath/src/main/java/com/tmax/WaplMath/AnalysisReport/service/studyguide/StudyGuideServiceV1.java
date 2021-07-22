package com.tmax.WaplMath.AnalysisReport.service.studyguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SkillStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumDataDetailDTO;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserExamScopeInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.curriculum.CurriculumServiceV0;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.Recommend.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Studyguide service v1 implementation
 * @author Jonghyun Seong
 */
@Slf4j
@Service("AR-StudyGuideServiceV1")
public class StudyGuideServiceV1 implements StudyGuideServiceBase{

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    CurriculumInfoRepo currInfoRepo;

    @Autowired
    @Qualifier("AR-UserInfoRepo")
    UserInfoRepo userInfoRepo;

    @Autowired
    @Qualifier("AR-UserExamScopeInfoRepo")
    UserExamScopeInfoRepo examScopeRepo;

    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    UserKnowledgeRepo userKnowledgeRepo;

    @Autowired
    LRSAPIManager lrsApiManager;

    @Autowired
    CurriculumServiceV0 currSvc;

    @Autowired
    ExamScopeUtil examScopeUtil;


    @Override
    public StudyGuideDTO getStudyGuideOfUser(String userID) {
        //Get LRS statement list for user
        List<String> actionTypeList = Arrays.asList("submit", "start");
        List<String> sourceTypeList = Arrays.asList("diagnosis", "diagnosis_simple");

        List<LRSStatementResultDTO> statementList = lrsApiManager.getUserStatement(userID, actionTypeList, sourceTypeList);

        //Build a chapter ID set from diagnosis data
        Set<String> diagnosisSecIdSet = new HashSet<>();
        for(LRSStatementResultDTO statement : statementList){
            if(statement.getSourceId() == null)
                continue;
            
            try {
                String currID = currInfoRepo.getCurrIdByProbId(Integer.valueOf(statement.getSourceId()));

                if(currID.length() < 14)// cannot be chapter
                    continue;

                //Cast to section id
                diagnosisSecIdSet.add(currSvc.castCurriculumID(currID, "section"));
            }
            catch (Throwable e){
                log.warn("Source ID is null. {}", userID);
            }
        }

        //Get all examscope currID (target range)
        Set<String> targetSecIDSet = examScopeUtil.getCurrIdListOfScope(userID)
                                                   .stream()
                                                   .flatMap(id ->{
                                                       if(id.length() < 14) //Cannot be chapter
                                                            return Stream.empty();
                                                        
                                                        return Stream.of(currSvc.castCurriculumID(id, "section"));
                                                   })
                                                   .collect(Collectors.toSet());
                                            
        //Remove target sections from diagnosis set
        diagnosisSecIdSet = diagnosisSecIdSet.stream().filter(id -> !targetSecIDSet.contains(id)).collect(Collectors.toSet());                                    

        //Get curr data of the list
        CurriculumDataDTO diagCurrData = currSvc.getByIdList(userID, 
                                                        new ArrayList<>(diagnosisSecIdSet), 
                                                        new HashSet<String>(Arrays.asList("ukKnowledgeList", "currDataList.ukIdList")) );
        
        

        //TODO use the currSvc sorting
        List<CurriculumDataDetailDTO> diagCurrList = diagCurrData.getCurrDataList();
        Collections.sort(diagCurrList, (a,b)-> a.getScore().getScore().compareTo(b.getScore().getScore()));

        //Get curr data of the list
        CurriculumDataDTO targetCurrData = currSvc.getByIdList(userID, 
                                                                new ArrayList<>(targetSecIDSet), 
                                                                new HashSet<String>(Arrays.asList("ukKnowledgeList", "currDataList.ukIdList")) );
        List<CurriculumDataDetailDTO> targetCurrList = targetCurrData.getCurrDataList();
        Collections.sort(targetCurrList, (a,b)-> a.getBasic().getSeq().compareTo(b.getBasic().getSeq())); //Sort target by seq ID

        //Merge 2 lists
        List<CurriculumDataDetailDTO> resultCurrDataList = Stream.of(diagCurrList, targetCurrList).flatMap(l -> l.stream()).collect(Collectors.toList());
        List<ChapterDetailDTO> chapterDetailList = resultCurrDataList.stream().map(curr -> createDetailfromCurrData(curr)).collect(Collectors.toList());

        return StudyGuideDTO.builder()
                            .chapterDetailList(chapterDetailList)
                            .build();
    }

    //Create chapterDetailList from currData List
    private ChapterDetailDTO createDetailfromCurrData(CurriculumDataDetailDTO currData){
        String type = "";
        Integer idLen = currData.getBasic().getId().length();

        if(idLen == 11)
            type = "대단원";
        else if(idLen == 14)
            type = "중단원";
        else if(idLen == 17)
            type = "소단원";

        List<Float> percentileLUT = currData.getStats().getPercentile();
        int lutSize = percentileLUT.size();

        int idx50 = Math.min(lutSize - 1, (int)Math.floor(0.5 * lutSize) );
        int idx90 = Math.min(lutSize - 1, (int)Math.floor(0.9 * lutSize) );

        return ChapterDetailDTO.builder()
                               .name(currData.getBasic().getName())
                               .id(currData.getBasic().getId())
                               .imagePath(null)
                               .sequence(currData.getBasic().getSeq())
                               .type(type)
                               .skillData(SkillStatDTO.builder()
                                                      .user(currData.getScore().getScore())
                                                      .userpercentile(currData.getScore().getPercentile())
                                                      .waplscore(currData.getWaplscore().getScore())
                                                      .waplscorepercentile(currData.getWaplscore().getPercentile())
                                                      .average(percentileLUT.get(idx50))
                                                      .top10Tier(percentileLUT.get(idx90))
                                                      .globalstd(currData.getStats().getStd())
                                                      .build())
                               .build();
    }
}

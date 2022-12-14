package com.tmax.WaplMath.Recommend.util.waplscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.model.problem.ProblemType;
import com.tmax.WaplMath.Common.util.error.CommonErrorCode;
import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbDTOV2;
import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbListDTOV2;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.util.ExamScope;
import com.tmax.WaplMath.Recommend.util.config.CardConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Generate expected schedule type list for calculate wapl score v2
 * @author Sangheon_lee
 * @since 2021-10-28
 */
@Slf4j
@Component
public class WaplScoreManagerV2 {
  
  //Logging option
  private boolean PRINT_CARD_INFO = true; //level 1
  private boolean PRINT_TYPE_INFO = false; //level 2
  private boolean PRINT_PROB_INFO = false; //level 3

  @Autowired
  @Qualifier("RE-ProblemTypeRepo")
  ProblemTypeRepo problemTypeRepo;

  @Autowired
  @Qualifier("RE-CurriculumRepo")
  CurriculumRepo curriculumRepo;


  private Integer totalTypeLength = 0;

  private Integer totalProbCnt = 0;


  public void setTestMode(){
    this.PRINT_CARD_INFO = false;
    this.PRINT_TYPE_INFO = false;
    this.PRINT_PROB_INFO = false;
  }


  public List<WaplScoreProbDTOV2> generateSectionCardProbList(Set<String> sectionIdSet, String cardType, Integer probNum) {

    List<WaplScoreProbDTOV2> cardProbDTOList = new ArrayList<>();

    List<String> midSubSectionList = curriculumRepo.findSubSectionListInSectionSet(sectionIdSet);

    // subSection --> type
    Map<String, List<Integer>> subSectionTypeMap = new HashMap<>();
    for (String subSection : midSubSectionList)
      subSectionTypeMap.put(subSection, problemTypeRepo.findTypeIdListInSubSection(subSection));

    Collections.shuffle(midSubSectionList);	
    
    int probCnt = 0;
    while (probCnt != probNum) {
      String subSection = midSubSectionList.get(probCnt % midSubSectionList.size());
      List<Integer> typeList = subSectionTypeMap.get(subSection);      
      Integer typeId = typeList.get(new Random().nextInt(typeList.size()));
      
      if (PRINT_PROB_INFO)	log.info("\t????????? ?????? ?????? {}, ??????={}", probCnt + 1, typeId);
      cardProbDTOList.add(new WaplScoreProbDTOV2(typeId, cardType));
      probCnt += 1;
    }

    totalProbCnt += probCnt;
    if (PRINT_TYPE_INFO)	log.info("\tprobDTO ??????: {}", cardProbDTOList);
    return cardProbDTOList;
  }


  public List<WaplScoreProbDTOV2> generateTrialExamCardProbList(List<String> examSubSectionList) {

    List<WaplScoreProbDTOV2> cardProbDTOList = new ArrayList<>();

    List<Integer> typeList = problemTypeRepo.findTypeIdListInSubSectionList(examSubSectionList);

    int probCnt = 0;
    while (probCnt != CardConstants.MAX_CARD_PROB_NUM) {
      Integer typeId = typeList.get(new Random().nextInt(typeList.size()));
      
      if (PRINT_PROB_INFO)	log.info("\t???????????? ?????? ?????? {}, ??????={}", probCnt + 1, typeId);
      cardProbDTOList.add(new WaplScoreProbDTOV2(typeId, "????????????"));
      probCnt += 1;
    }

    totalProbCnt += probCnt;
    if (PRINT_TYPE_INFO)	log.info("\tprobDTO ??????: {}", cardProbDTOList);
    return cardProbDTOList;
  }


  public WaplScoreProbListDTOV2 getWaplScoreProbList(String targetExam, String currentCurriculumId, Integer remainDay) {

    WaplScoreProbListDTOV2 output = new WaplScoreProbListDTOV2();
    List<WaplScoreProbDTOV2> probList = new ArrayList<>();
    
    this.totalTypeLength = 0;
    this.totalProbCnt = 0;

    if (remainDay <= 0)
      throw new GenericInternalException(CommonErrorCode.INVALID_ARGUMENT, 
                                         "[WaplScoreManager] 'remainDay' must not be less than 0 ");

    //?????? ??????
    if (remainDay > 14) {

      Integer normalScheduleDay = remainDay - 14;
      String endCurrId = ExamScope.examScope.get(targetExam.replace("mid", "final")).get(1);

      List<String> subSectionList = curriculumRepo.findSubSectionListBetween(currentCurriculumId, endCurrId); // ???????????????????????????
      List<ProblemType> typeIdList = problemTypeRepo.findTypeListInSubSectionList(subSectionList);
      Set<String> sectionIdSet = new HashSet<>(); // ???????????????????????????

      subSectionList.forEach(subSection -> sectionIdSet.add(subSection.substring(0, 14)));
      if (PRINT_CARD_INFO) log.info("typeId ?????? = {}", typeIdList.size());

      String prevSection = typeIdList.get(0).getCurriculumId().substring(0, 14);
      Integer nextTypeIdx = 0;
      boolean isTypeFinished = false;

      for (int cnt = 0; cnt < normalScheduleDay; cnt++) {
        int probCnt = 0;
        List<WaplScoreProbDTOV2> todayCardProbDTOList = new ArrayList<>();
        int cardCnt = 1;
        if (nextTypeIdx == typeIdList.size())	nextTypeIdx--;

        ProblemType firstType = typeIdList.get(nextTypeIdx);
        String currentSection = firstType.getCurriculumId().substring(0, 14);
        
        //????????? ??????
        if (!currentSection.equals(prevSection) || isTypeFinished) {
          if (PRINT_CARD_INFO)	log.info("???????????? {}?????? ??? ?????? ?????? ?????? (?????????={})", cnt + 1, prevSection);
          probCnt += CardConstants.MAX_CARD_PROB_NUM;
          todayCardProbDTOList = generateSectionCardProbList(new HashSet<>(Arrays.asList(prevSection)), 
                                                             "????????????", CardConstants.MAX_CARD_PROB_NUM);
          if (isTypeFinished) isTypeFinished = false;
        } 
        
        // ?????? ??????
        else if (nextTypeIdx < typeIdList.size() - 1) {
          for (ProblemType type : typeIdList.subList(nextTypeIdx, typeIdList.size())) {
            Integer typeId = type.getTypeId();

            if (PRINT_CARD_INFO)	
              log.info("???????????? {}?????? ??? {}?????? ?????? ?????? (??????={}, ?????????={})", cnt + 1, cardCnt++, typeId, type.getCurriculumId());
              
            for (int i = 0; i < CardConstants.MAX_TYPE_CARD_PROB_NUM; i++)
              todayCardProbDTOList.add(new WaplScoreProbDTOV2(typeId, "????????????"));

            probCnt += CardConstants.MAX_TYPE_CARD_PROB_NUM;
            totalProbCnt += CardConstants.MAX_TYPE_CARD_PROB_NUM;

            nextTypeIdx += 1;
            if (probCnt >= CardConstants.MAX_CARD_PROB_NUM)
              break;
          }

          if (nextTypeIdx == typeIdList.size())
            isTypeFinished = true;
        }

        // ?????? ????????????
        if (probCnt < CardConstants.MAX_CARD_PROB_NUM) {
          Integer addtlProbNum = CardConstants.MAX_CARD_PROB_NUM - probCnt;
          if (PRINT_CARD_INFO)	log.info("???????????? {}?????? ??? ?????? ???????????? ?????? ({}??????)", cnt + 1, addtlProbNum);
          todayCardProbDTOList.addAll(generateSectionCardProbList(sectionIdSet, "????????????", addtlProbNum));
        }

        probList.addAll(todayCardProbDTOList);
        prevSection = currentSection;
        if (PRINT_CARD_INFO) log.info("");
      }
    }
    if (PRINT_CARD_INFO)	log.info("Normal schedule probNum = {}", totalProbCnt);
    if (PRINT_CARD_INFO)	log.info("=========================");


    //?????? ??????
    String examStartCurriculumId = ExamScope.examScope.get(targetExam).get(0);
    String examEndCurriculumId = ExamScope.examScope.get(targetExam).get(1);

    List<String> examSubSectionList = 
      curriculumRepo.findSubSectionListBetween(examStartCurriculumId, examEndCurriculumId); // ?????? ??????
    Set<String> sectionIdSet = new HashSet<>();
    examSubSectionList.forEach(subSection -> sectionIdSet.add(subSection.substring(0, 14)));

    if (PRINT_CARD_INFO) log.info("???????????? ?????????: {}", sectionIdSet);
    int examDay = 0;    
    Integer numSectionExamCards = Math.floorDiv(14, sectionIdSet.size());
    Map<String, Integer> sectionExamCardNums = new HashMap<>();
    sectionIdSet.forEach(sectionId -> sectionExamCardNums.put(sectionId, numSectionExamCards));
    
    // prob list for each exam day
    List<List<WaplScoreProbDTOV2>> dayExamProbList = new ArrayList<>();
    while (examDay != 14) {

      // trial exam card
      if (examDay >= 12) {
        if (PRINT_CARD_INFO) log.info("???????????? {}?????? ??? ???????????? ??????", examDay + 1);
        dayExamProbList.add(generateTrialExamCardProbList(examSubSectionList));
      } 
      
      // section exam card
      else if (!sectionExamCardNums.keySet().isEmpty()) {
        String sectionId = curriculumRepo.sortByCurrSeq(sectionExamCardNums.keySet()).get(0);
        if (PRINT_CARD_INFO) log.info("???????????? {}?????? ??? Type1 ?????? (?????????={})", examDay + 1, sectionId);
        dayExamProbList.add(generateSectionCardProbList(new HashSet<>(Arrays.asList(sectionId)), 
                                                    "????????????", CardConstants.MAX_CARD_PROB_NUM));
        if (sectionExamCardNums.get(sectionId) == 1)
          sectionExamCardNums.remove(sectionId);
        else
          sectionExamCardNums.put(sectionId, sectionExamCardNums.get(sectionId)-1);
      } 

      // total exam card
      else {
        if (PRINT_CARD_INFO) log.info("???????????? {}?????? ??? Type2 ?????? (?????????={})", examDay + 1, sectionIdSet);
        dayExamProbList.add(generateSectionCardProbList(sectionIdSet, "????????????", CardConstants.MAX_CARD_PROB_NUM));				
      }
      examDay += 1;
    }

    // if remain day less than 14 days
    Collections.reverse(dayExamProbList);

    Integer listSize = Math.min(remainDay, 14);
    if (PRINT_CARD_INFO)	log.info("Cut exam card within {} days", listSize);

    dayExamProbList.subList(0, listSize).stream()
                   .forEach(examProbList -> probList.addAll(examProbList));

    if (PRINT_CARD_INFO)	log.info("Total waplScore probNum = {}", probList.size());
    
    probList.forEach(e -> totalTypeLength += 1);
    if (PRINT_CARD_INFO)	log.info("Total waplScore type sequence length = {}", totalTypeLength);

    output.setProbList(probList);
    return output;
  }
}

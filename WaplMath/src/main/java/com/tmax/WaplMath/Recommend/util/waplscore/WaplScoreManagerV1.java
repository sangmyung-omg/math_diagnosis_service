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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.tmax.WaplMath.Common.model.problem.ProblemType;
import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbDTO;
import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbListDTO;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.TypeUkRelRepo;
import com.tmax.WaplMath.Recommend.util.ExamScope;
import com.tmax.WaplMath.Recommend.util.config.CardConstants;

/**
 * Generate expected schedule uk list for calculate wapl score v1
 * @author Sangheon_lee
 * @since 2021-07-05
 */
@Slf4j
@Component
public class WaplScoreManagerV1 {

  //Logging option
  private final Boolean printCardInfo = false; //level 1
  private final Boolean printUkInfo = false; //level 2
  private final Boolean printProbInfo = false; //level 3


  @Autowired
  @Qualifier("RE-TypeUkRelRepo")
  TypeUkRelRepo typeUkRelRepo;

  @Autowired
  @Qualifier("RE-ProblemTypeRepo")
  ProblemTypeRepo problemTypeRepo;

  @Autowired
  @Qualifier("RE-CurriculumRepo")
  CurriculumRepo curriculumRepo;


  public Integer totalUkLength = 0;

  public Integer totalProbCnt = 0;
  

  public List<WaplScoreProbDTO> generateSectionCardProbList(Set<String> sectionIdSet, String type, Integer probNum) {
    List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<>();
    List<String> midSubSectionList = curriculumRepo.findSubSectionListInSectionSet(sectionIdSet);
    Map<String, List<Integer>> subSectionTypeMap = new HashMap<String, List<Integer>>();
    for (String subSection : midSubSectionList)
      subSectionTypeMap.put(subSection, problemTypeRepo.findTypeIdListInSubSection(subSection));
    Collections.shuffle(midSubSectionList);	
    int probCnt = 0;
    while (probCnt != probNum) {
      String subSection = midSubSectionList.get(probCnt % midSubSectionList.size());
      List<Integer> typeList = subSectionTypeMap.get(subSection);
      Integer typeId = typeList.get(new Random().nextInt(typeList.size()));
      List<Integer> ukList = typeUkRelRepo.findAllUkByTypeId(typeId);
      if (ukList.size() != 0) {
        if (printProbInfo)	log.info("	중단원 세트 문제 {}, 유형={}, uk추가: {}", probCnt + 1, typeId, ukList);
        cardProbDTOList.add(new WaplScoreProbDTO(ukList, type));
        probCnt += 1;
      }
    }
    totalProbCnt += probCnt;
    if (printUkInfo)	log.info("	probDTO 추가: {}", cardProbDTOList);
    return cardProbDTOList;
  }

  public List<WaplScoreProbDTO> generateTrialExamCardProbList(List<String> examSubSectionList) {
    List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<WaplScoreProbDTO>();
    List<Integer> typeList = problemTypeRepo.findTypeIdListInSubSectionList(examSubSectionList);

    int probCnt = 0;
    while (probCnt != CardConstants.MAX_CARD_PROB_NUM) {
      Integer typeId = typeList.get(new Random().nextInt(typeList.size()));
      List<Integer> ukList = typeUkRelRepo.findAllUkByTypeId(typeId);
      if (ukList.size() != 0) {
        if (printProbInfo)	log.info("	모의고사 카드 문제 {}, 유형={}, uk추가: {}", probCnt + 1, typeId, ukList);
        cardProbDTOList.add(new WaplScoreProbDTO(ukList, "시험대비"));
        probCnt += 1;
      }
    }
    totalProbCnt += probCnt;
    if (printUkInfo)	log.info("	probDTO 추가: {}", cardProbDTOList);
    return cardProbDTOList;
  }

  public WaplScoreProbListDTO getWaplScoreProbList(String targetExam, String currentCurriculumId, Integer remainDay) {
    WaplScoreProbListDTO output = new WaplScoreProbListDTO();
    List<WaplScoreProbDTO> probList = new ArrayList<WaplScoreProbDTO>();

    //실력 향상
    if (remainDay > 14) {
      Integer normalScheduleDay = remainDay - 14;
      String EndCurrId = ExamScope.examScope.get(targetExam.replace("mid", "final")).get(1);
      List<String> subSectionList = curriculumRepo.findSubSectionListBetween(currentCurriculumId, EndCurrId); // 이번학기마지막까지
      List<ProblemType> typeIdList = problemTypeRepo.findTypeListInSubSectionList(subSectionList);
      Set<String> sectionIdSet = new HashSet<String>(); // 이번학기마지막까지
      subSectionList.forEach(subSection -> sectionIdSet.add(subSection.substring(0, 14)));
      if (printCardInfo) log.info("typeId 개수 = {}", typeIdList.size());

      String prevSection = typeIdList.get(0).getCurriculumId().substring(0, 14);
      Integer nextTypeIdx = 0;
      Boolean isTypeFinished = false;
      for (int cnt = 0; cnt < normalScheduleDay; cnt++) {
        int probCnt = 0;
        List<WaplScoreProbDTO> todayCardProbDTOList = new ArrayList<WaplScoreProbDTO>();
        int cardCnt = 1;
        if (nextTypeIdx == typeIdList.size())	nextTypeIdx--;
        ProblemType firstType = typeIdList.get(nextTypeIdx);
        String currentSection = firstType.getCurriculumId().substring(0, 14);
        //중단원 평가
        if (!currentSection.equals(prevSection) || isTypeFinished) {
          if (printCardInfo)	log.info("실력향상 {}번째 날 중간 평가 카드 (중단원={})", cnt + 1, prevSection);
          probCnt += CardConstants.MAX_CARD_PROB_NUM;
          todayCardProbDTOList = generateSectionCardProbList(new HashSet<String>(Arrays.asList(prevSection)), "실력향상", CardConstants.MAX_CARD_PROB_NUM);
          if (isTypeFinished) isTypeFinished = false;
        } else if (nextTypeIdx < typeIdList.size() - 1) {
          for (ProblemType type : typeIdList.subList(nextTypeIdx, typeIdList.size())) {
            Integer typeId = type.getTypeId();
            List<Integer> ukList = typeUkRelRepo.findAllUkByTypeId(typeId);
            if(ukList.size() != 0) {
              if (printCardInfo)	log.info("실력향상 {}번째 날 {}번째 유형 카드 (유형={}, 소단원={}) uk 추가: {}", cnt + 1, cardCnt++, typeId, type.getCurriculumId(), ukList);
              for (int i = 0; i < CardConstants.MAX_TYPE_CARD_PROB_NUM; i++)
                todayCardProbDTOList.add(new WaplScoreProbDTO(ukList, "실력향상"));
              probCnt += CardConstants.MAX_TYPE_CARD_PROB_NUM;
              totalProbCnt += CardConstants.MAX_TYPE_CARD_PROB_NUM;
            }
            nextTypeIdx += 1;
            if (probCnt >= CardConstants.MAX_CARD_PROB_NUM)
              break;
          }
          if (nextTypeIdx == typeIdList.size())
            isTypeFinished = true;
        }
        if (probCnt < CardConstants.MAX_CARD_PROB_NUM) {
          Integer addtlProbNum = CardConstants.MAX_CARD_PROB_NUM - probCnt;
          if (printCardInfo)	log.info("실력향상 {}번째 날 추가 보충학습 카드 ({}문제)", cnt + 1, addtlProbNum);
          todayCardProbDTOList.addAll(generateSectionCardProbList(sectionIdSet, "시험대비", addtlProbNum));
        }
        probList.addAll(todayCardProbDTOList);
        prevSection = currentSection;
        if (printCardInfo) log.info("");
      }
    }
    log.info("실력향상 문제 수 = {}", totalProbCnt);
    log.info("=========================");

    //시험 대비
    String examStartCurriculumId = ExamScope.examScope.get(targetExam).get(0);
    String examEndCurriculumId = ExamScope.examScope.get(targetExam).get(1);
    List<String> examSubSectionList = curriculumRepo.findSubSectionListBetween(examStartCurriculumId, examEndCurriculumId); // 시험 범위
    Set<String> sectionIdSet = new HashSet<String>();
    examSubSectionList.forEach(subSection -> sectionIdSet.add(subSection.substring(0, 14)));

    if (printCardInfo) log.info("시험대비 중단원: {}", sectionIdSet);
    int examDay = 0;
    Integer numExamType1Cards = Math.floorDiv(14, sectionIdSet.size());
    Map<String, Integer> type1CardsNum = new HashMap<String, Integer>();
    sectionIdSet.forEach(sectionId -> type1CardsNum.put(sectionId, numExamType1Cards));
    
    while (examDay != 14) {
      List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<WaplScoreProbDTO>();
      // 모의고사 이틀
      if (examDay >= 12) {
        if (printCardInfo) log.info("시험대비 {}번째 날 모의고사 카드", examDay + 1);
        cardProbDTOList = generateTrialExamCardProbList(examSubSectionList);
        probList.addAll(cardProbDTOList);
      // type 1
      } else if (type1CardsNum.keySet().size() != 0) {
        String sectionId = curriculumRepo.sortByCurrSeq(type1CardsNum.keySet()).get(0);
        if (printCardInfo) log.info("시험대비 {}번째 날 Type1 카드 (중단원={})", examDay + 1, sectionId);
        cardProbDTOList = generateSectionCardProbList(new HashSet<String>(Arrays.asList(sectionId)), "시험대비", CardConstants.MAX_CARD_PROB_NUM);
        probList.addAll(cardProbDTOList);
        if (type1CardsNum.get(sectionId) == 1)
          type1CardsNum.remove(sectionId);
        else
          type1CardsNum.put(sectionId, type1CardsNum.get(sectionId)-1);
      // type 2
      } else {
        if (printCardInfo) log.info("시험대비 {}번째 날 Type2 카드 (중단원={})", examDay + 1, sectionIdSet);
        cardProbDTOList = generateSectionCardProbList(sectionIdSet, "시험대비", CardConstants.MAX_CARD_PROB_NUM);
        probList.addAll(cardProbDTOList);				
      }
      examDay += 1;
    }

    log.info("최종 문제 수 = {}", totalProbCnt);
    assert (totalProbCnt == probList.size());
    output.setProbList(probList);
    probList.forEach(e -> totalUkLength += e.getUkList().size());
    log.info("최종 UK sequence 길이 = {}", totalUkLength);

    return output;
  }
}
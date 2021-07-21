package com.tmax.WaplMath.Recommend.service.schedule.v2;

import java.util.ArrayList;
import java.util.List;
import com.tmax.WaplMath.Common.util.exception.StackPrinter;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTOV2;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleCardOutputDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepo;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
import com.tmax.WaplMath.Recommend.util.card.CardGeneratorV2;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleConfiguratorV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Generate today normal/exam schedule card v2
 * @author Sangheon_lee
 * @since 2021-06-30
 */

/*
* ┌────────────────┐
* │                │
* │     <START>    │
* │                │
* └────────┬───────┘
*          │
*          │
* ┌────────▼───────┐      ┌───────────────────────────┐
* │                │      │                           │
* │     Check      ├─────►│         Generate          │
* │ USER KNOWLEDGE │      │   Schedule Configuration  │
* │                │      │                           │
* └────────────────┘      └─────────────┬─────────────┘
*                                       │
*                                       │
*                         ┌─────────────▼─────────────┐
*                         │                           │
*                         │         Generate          │
*                         │      Schedule Cards       │
*                         │                           │
*                         └───────────────────────────┘
*/

@Slf4j
@Service("ScheduleServiceV2")
public class ScheduleServiceV2 implements ScheduleServiceBaseV2 {

  @Autowired
  CardGeneratorV2 cardGenerator = new CardGeneratorV2();

  @Autowired
  ScheduleConfiguratorV2 scheduleConfigurator = new ScheduleConfiguratorV2();

  @Autowired
  @Qualifier("RE-UserKnowledgeRepo")
  private UserKnowledgeRepo userKnowledgeRepo;
  

  // 21.07.21. Throw exception if mastery not exist
  public void checkUserMasteryExist(String userId) {

    if (!userKnowledgeRepo.findExistUserList().contains(userId)){
      log.error("Not exist mastery for user = {} ", userId);
      
      throw new RecommendException(RecommendErrorCode.USER_MASTERY_NOT_EXIST_ERROR);
    }
  }


  public ScheduleCardOutputDTO getScheduleCard(String userId, String type){

    List<CardDTOV2> cardList = new ArrayList<>();

    // check user mastery exist in USER_KNOWLEDGE
    checkUserMasteryExist(userId);

    // set schedule configurator
    scheduleConfigurator.setUserValue(userId);   
     
    // get schedule configuration
    ScheduleConfigDTO scheduleConfig;
    try{
      if (type.equals("normal"))
        scheduleConfig = scheduleConfigurator.getNormalScheduleConfig();

      else if (type.equals("exam"))
        scheduleConfig = scheduleConfigurator.getExamScheduleConfig();

      else
        scheduleConfig = scheduleConfigurator.getDummyScheduleConfig();
    } 
    catch (Throwable e){
      throw new RecommendException(RecommendErrorCode.SCHEDULE_CONFIGURATOR_ERROR, StackPrinter.getStackTrace(e));
    }

    // set card generator
    cardGenerator.setUserValue(userId, 
                               scheduleConfigurator.getSolvedProbIdSet(), 
                               scheduleConfigurator.getExamSubSectionIdSet());
    
    // generate schedule cards with schedule config
    try{
      scheduleConfig.getCardConfigList().forEach(config -> cardList.add(cardGenerator.generateCard(config)));
    }
    catch (Throwable e){
      throw new RecommendException(RecommendErrorCode.CARD_GENERATOR_ERROR, StackPrinter.getStackTrace(e));
    }

    // if cards empty = no probs to serve
    if (cardList.isEmpty())
      throw new RecommendException(RecommendErrorCode.CARD_GENERATE_NO_CARDS_ERROR);

    return ScheduleCardOutputDTO.builder()
                                .cardList(cardList)
                                .message(String.format("Successfully return %s cards list.", type))
                                .build();
  }

  @Override
  public ScheduleCardOutputDTO getNormalScheduleCard(String userId) {
    return getScheduleCard(userId, "normal");		
  }

  @Override
  public ScheduleCardOutputDTO getExamScheduleCard(String userId) {
    return getScheduleCard(userId, "exam");
  }

  @Override
  public ScheduleCardOutputDTO getScheduleCardDummy(String userId) {
    return getScheduleCard(userId, "dummy");
  }

}

package com.tmax.WaplMath.Recommend.service.schedule;

import java.util.ArrayList;
import java.util.List;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleCardOutputDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepo;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
import com.tmax.WaplMath.Recommend.util.card.CardGenerator;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

@Service("ScheduleServiceV2")
public class ScheduleServiceV2 implements ScheduleServiceBaseV2 {

  @Autowired
  CardGenerator cardGenerator;

  @Autowired
  ScheduleConfigurator scheduleConfigurator;

  @Autowired
  @Qualifier("RE-UserKnowledgeRepo")
  private UserKnowledgeRepo userKnowledgeRepo;
  

  // 21.07.21. Throw exception if user mastery not exist
  public void checkUserMasteryExist(String userId) {

    if (!userKnowledgeRepo.findExistUserList().contains(userId))      
      throw new RecommendException(RecommendErrorCode.USER_MASTERY_NOT_EXIST_ERROR, userId);
  }


  // type = "normal", "dummy", "exam"
  public ScheduleCardOutputDTO getScheduleCard(String userId, String type, boolean debugMode){

    List<CardDTO> cardList = new ArrayList<>();

    // check user mastery exist in USER_KNOWLEDGE TB
    checkUserMasteryExist(userId);

    // initialize schedule configurator
    scheduleConfigurator.initConfigurator(userId);   
     
    // get schedule configuration
    ScheduleConfigDTO scheduleConfig;
    if (type.equals("normal"))
      scheduleConfig = scheduleConfigurator.getNormalScheduleConfig();

    else if (type.equals("exam"))
      scheduleConfig = scheduleConfigurator.getExamScheduleConfig();

    else
      scheduleConfig = scheduleConfigurator.getDummyScheduleConfig();

    // initialize card generator
    cardGenerator.initGenerator(userId,
                                scheduleConfigurator.getSolvedProbIdSet(), 
                                scheduleConfigurator.getExamSubSectionIdSet());
    
    // set log level for debug mode
    cardGenerator.setLogLevel(debugMode);

    // if configs empty = no probs to serve
    if (scheduleConfig.getCardConfigList().isEmpty())
      throw new RecommendException(RecommendErrorCode.CARD_GENERATE_NO_CARDS_ERROR, userId);

    // generate schedule cards with schedule config
    scheduleConfig.getCardConfigList().forEach(config -> cardList.add(cardGenerator.generateCard(config)));

    // if cards empty = no probs to serve
    if (cardList.isEmpty())
      throw new RecommendException(RecommendErrorCode.CARD_GENERATE_NO_CARDS_ERROR, userId);

    return ScheduleCardOutputDTO.builder()
                                .cardList(cardList)
                                .message(String.format("Successfully return %s cards list.", type))
                                .isScopeCompleted(scheduleConfig.getIsScopeCompleted())
                                .build();
  }

  @Override
  public ScheduleCardOutputDTO getNormalScheduleCard(String userId, boolean debugMode) {
    return getScheduleCard(userId, "normal", debugMode);		
  }

  @Override
  public ScheduleCardOutputDTO getExamScheduleCard(String userId, boolean debugMode) {
    return getScheduleCard(userId, "exam", debugMode);
  }

  @Override
  public ScheduleCardOutputDTO getScheduleCardDummy(String userId) {
    return getScheduleCard(userId, "dummy", false);
  }

}

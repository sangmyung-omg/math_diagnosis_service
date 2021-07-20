package com.tmax.WaplMath.Recommend.service.schedule.v2;

import java.util.ArrayList;
import java.util.List;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTOV2;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleCardOutputDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
import com.tmax.WaplMath.Recommend.util.card.CardGeneratorV2;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleConfiguratorV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Generate today normal/exam schedule card v2
 * @author Sangheon_lee
 * @since 2021-06-30
 */
@Slf4j
@Service("ScheduleServiceV2")
public class ScheduleServiceV2 implements ScheduleServiceBaseV2 {

  @Autowired
  CardGeneratorV2 cardGenerator = new CardGeneratorV2();

  @Autowired
  ScheduleConfiguratorV2 scheduleConfigurator = new ScheduleConfiguratorV2();
  

  public ScheduleCardOutputDTO getScheduleCard(String userId, String type){

    List<CardDTOV2> cardList = new ArrayList<>();

    scheduleConfigurator.setUserValue(userId);
    
    ScheduleConfigDTO scheduleConfig;

    if (type.equals("normal"))
      scheduleConfig = scheduleConfigurator.getNormalScheduleConfig();

    else if (type.equals("exam"))
      scheduleConfig = scheduleConfigurator.getExamScheduleConfig();

    else
      scheduleConfig = scheduleConfigurator.getDummyScheduleConfig();

    cardGenerator.setUserValue(userId, 
                               scheduleConfigurator.getSolvedProbIdSet(), 
                               scheduleConfigurator.getExamSubSectionIdSet());
    
    scheduleConfig.getCardConfigList().forEach(config -> cardList.add(cardGenerator.generateCard(config)));

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
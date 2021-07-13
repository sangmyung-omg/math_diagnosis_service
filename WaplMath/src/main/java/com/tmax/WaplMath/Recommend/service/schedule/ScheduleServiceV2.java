package com.tmax.WaplMath.Recommend.service.schedule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTOV2;
import com.tmax.WaplMath.Recommend.dto.schedule.NormalScheduleCardDTOV2;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.util.schedule.CardGeneratorV2;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleConfiguratorV2;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleHistoryManagerV1;

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
	@Autowired
	ScheduleHistoryManagerV1 historyManager;
	
	@Override
	public NormalScheduleCardDTOV2 getExamScheduleCard(String userId) {
		NormalScheduleCardDTOV2 output = new NormalScheduleCardDTOV2();
		List<CardDTOV2> cardList = new ArrayList<CardDTOV2>();
		ScheduleConfigDTO scheduleConfig;
		try {
			scheduleConfig = scheduleConfigurator.getExamScheduleConfig(userId);
		} catch (Exception e) {
			e.printStackTrace();
			output.setMessage("schedule configuration failure. " + e.getMessage());
			return output;
		}
		cardGenerator.userId = userId;
		cardGenerator.setSolvedProbIdSet(scheduleConfigurator.getSolvedProbIdSet());
		cardGenerator.setExamSubSectionIdSet(scheduleConfigurator.getExamSubSectionIdSet());
		CardDTOV2 card;
		log.info("소단원: {}", scheduleConfig.getAddtlSubSectionIdSet());
		for (CardConfigDTO cardConfig : scheduleConfig.getCardConfigList()) {
			card = cardGenerator.generateCard(cardConfig);
			cardList.add(card);
		}
		if (cardList.size() == 0) {
			output.setMessage("No cards were created. User seems to have solved all the problems.");
			return output;
		}
		output.setCardList(cardList);
		output.setMessage("Successfully return curriculum card list.");
		return output;
	}

	@Override
	public NormalScheduleCardDTOV2 getNormalScheduleCard(String userId) {
		NormalScheduleCardDTOV2 output = new NormalScheduleCardDTOV2();
		List<CardDTOV2> cardList = new ArrayList<CardDTOV2>();
		ScheduleConfigDTO scheduleConfig;
		try {
			scheduleConfig = scheduleConfigurator.getNormalScheduleConfig(userId);
		} catch (Exception e) {
			e.printStackTrace();
			output.setMessage("schedule configuration failure. " + e.getMessage());
			return output;
		}
		cardGenerator.userId = userId;
		cardGenerator.setSolvedProbIdSet(scheduleConfigurator.getSolvedProbIdSet());
		CardDTOV2 card;
		log.info("소단원: {}", scheduleConfig.getAddtlSubSectionIdSet());
		for (CardConfigDTO cardConfig : scheduleConfig.getCardConfigList()) {
			card = cardGenerator.generateCard(cardConfig);
			cardList.add(card);
		}
		if (cardList.size() == 0) {
			output.setMessage("No cards were created. User seems to have solved all the problems.");
			return output;
		}
		output.setCardList(cardList);
		output.setMessage("Successfully return curriculum card list.");
		return output;
	}

	@Override
	public NormalScheduleCardDTOV2 getNormalScheduleCardDummy(String userId) {
		NormalScheduleCardDTOV2 output = new NormalScheduleCardDTOV2();
		List<CardDTOV2> cardList = new ArrayList<CardDTOV2>();
		ScheduleConfigDTO scheduleConfig;
		try {
			scheduleConfig = scheduleConfigurator.getDummyScheduleConfig(userId);
		} catch (Exception e) {
			e.printStackTrace();
			output.setMessage("schedule configuration failure. " + e.getMessage());
			return output;
		}
		cardGenerator.userId = userId;
		cardGenerator.setSolvedProbIdSet(scheduleConfigurator.getSolvedProbIdSet());
		cardGenerator.setExamSubSectionIdSet(scheduleConfigurator.getExamSubSectionIdSet());
		CardDTOV2 card;
		log.info("소단원: {}", scheduleConfig.getAddtlSubSectionIdSet());
		for (CardConfigDTO cardConfig : scheduleConfig.getCardConfigList()) {
			card = cardGenerator.generateCard(cardConfig);
			cardList.add(card);
		}
		output.setCardList(cardList);
		output.setMessage("Successfully return curriculum card list.");
		return output;

	}

}

package com.tmax.WaplMath.Recommend.service.schedule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTOV1;
import com.tmax.WaplMath.Recommend.dto.schedule.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.NormalScheduleCardDTOV1;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.util.schedule.CardGeneratorV1;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleConfiguratorV1;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleHistoryManagerV1;

/**
 * Generate today normal/exam schedule card v1
 * @author Sangheon_lee
 */
@Slf4j
@Service("ScheduleServiceV1")
public class ScheduleServiceV1 implements ScheduleServiceBaseV1 {

	@Autowired
	CardGeneratorV1 cardGenerator = new CardGeneratorV1();
	@Autowired
	ScheduleConfiguratorV1 scheduleConfigurator = new ScheduleConfiguratorV1();
	@Autowired
	ScheduleHistoryManagerV1 historyManager;

	@Override
	public NormalScheduleCardDTOV1 getNormalScheduleCard(String userId) {
		NormalScheduleCardDTOV1 output = new NormalScheduleCardDTOV1();
		List<CardDTOV1> cardList = new ArrayList<CardDTOV1>();
		ScheduleConfigDTO scheduleConfig;
		try {
			scheduleConfig = scheduleConfigurator.getNormalScheduleConfig(userId);
		} catch (Exception e) {
			output.setMessage("schedule configuration failure. " + e.getMessage());
			return output;
		}
		cardGenerator.userId = userId;
		cardGenerator.setSolvedProbIdSet(scheduleConfigurator.getSolvedProbIdSet());
		CardDTOV1 card;
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
	// set to dummy --> 4개 카드 종류별로 return
	public NormalScheduleCardDTOV1 getNormalScheduleCardDummy(String userId) {
		NormalScheduleCardDTOV1 output = new NormalScheduleCardDTOV1();
		List<CardDTOV1> cardList = new ArrayList<CardDTOV1>();
		ScheduleConfigDTO scheduleConfig;
		try {
			scheduleConfig = scheduleConfigurator.getDummyScheduleConfig(userId);
		} catch (Exception e) {
			output.setMessage("schedule configuration failure. " + e.getMessage());
			return output;
		}
		cardGenerator.userId = userId;
		cardGenerator.setSolvedProbIdSet(scheduleConfigurator.getSolvedProbIdSet());
		cardGenerator.setExamSubSectionIdSet(scheduleConfigurator.getExamSubSectionIdSet());
		CardDTOV1 card;
		log.info("소단원: {}", scheduleConfig.getAddtlSubSectionIdSet());
		for (CardConfigDTO cardConfig : scheduleConfig.getCardConfigList()) {
			card = cardGenerator.generateCard(cardConfig);
			cardList.add(card);
		}
		output.setCardList(cardList);
		output.setMessage("Successfully return curriculum card list.");
		return output;

	}

	@Override
	public ExamScheduleCardDTO getExamScheduleCard(String userId) {
		ExamScheduleCardDTO output = new ExamScheduleCardDTO();
		return output;
	}

}

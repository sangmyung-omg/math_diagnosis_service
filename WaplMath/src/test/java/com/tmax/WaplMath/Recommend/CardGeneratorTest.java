package com.tmax.WaplMath.Recommend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTOV1;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepo;
import com.tmax.WaplMath.Recommend.util.card.CardGeneratorV1;

@SpringBootTest
public class CardGeneratorTest {
	@Autowired
	CardGeneratorV1 cardGenerator;
	@Autowired
  @Qualifier("RE-UserKnowledgeRepo")
	private UserKnowledgeRepo userKnowledgeRepo;

	@Test
	public void generateCardTest() {
		String userId = "mkkang";
		cardGenerator.userId = userId;
		//중간평가
		//		CardDTO midCard = cardManager.generateMidExamCard("중등-중2-1학-03-01");
		//		System.out.println(midCard.getProbIdSetList());
		//		System.out.println(midCard.getProbIdSetList().size());
		//		System.out.println(midCard.getCardDetail());


		List<Integer> suppleTypeIdList = new ArrayList<Integer>(Arrays.asList());
		List<Integer> solvedTypeIdList = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 15, 17, 19));
		List<TypeMasteryDTO> lowMasteryTypeList = userKnowledgeRepo.findLowTypeMasteryList(userId, solvedTypeIdList, suppleTypeIdList, 0.4f);
		CardDTOV1 midCard = cardGenerator.generateSupplementCard(lowMasteryTypeList);
		System.out.println(midCard.getProbIdSetList());
		System.out.println(midCard.getProbIdSetList().size());
		System.out.println(midCard.getCardDetail());
	}

}

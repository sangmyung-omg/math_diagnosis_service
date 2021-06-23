package com.tmax.WaplMath.Recommend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTO;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;
import com.tmax.WaplMath.Recommend.util.schedule.CardManager;

@SpringBootTest
public class CardManagerTest {
	@Autowired
	CardManager cardManager;
	@Autowired
	private UserKnowledgeRepository userKnowledgeRepo;

	@Test
	public void generateCardTest() {
		String userId = "mkkang";
		cardManager.userId = userId;
		//중간평가
		//		CardDTO midCard = cardManager.generateMidExamCard("중등-중2-1학-03-01");
		//		System.out.println(midCard.getProbIdSetList());
		//		System.out.println(midCard.getProbIdSetList().size());
		//		System.out.println(midCard.getCardDetail());


		List<Integer> suppleTypeIdList = new ArrayList<Integer>(Arrays.asList());
		List<Integer> solvedTypeIdList = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 15, 17, 19));
		List<TypeMasteryDTO> lowMasteryTypeList = userKnowledgeRepo.findLowTypeMasteryList(userId, solvedTypeIdList, suppleTypeIdList, 0.4f);
		CardDTO midCard = cardManager.generateSupplementCard(lowMasteryTypeList);
		System.out.println(midCard.getProbIdSetList());
		System.out.println(midCard.getProbIdSetList().size());
		System.out.println(midCard.getCardDetail());
	}

}

package com.tmax.WaplMath.Recommend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbListDTO;
import com.tmax.WaplMath.Recommend.util.schedule.WaplScoreManager;

@SpringBootTest
public class WaplScoreManagerTest {

	@Autowired
	WaplScoreManager waplScore;

	@Test
	public void getScheduleUkListTest() {
		//given

		//when
		WaplScoreProbListDTO ukList = waplScore.getWaplScoreProbList("1-2-final", "중등-중1-1학-03-01-01", 18);

		//then
		//System.out.println(ukList);
	}
}

package com.tmax.WaplMath.Recommend;

import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbListDTOV2;
import com.tmax.WaplMath.Recommend.util.waplscore.WaplScoreManagerV2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WaplScoreManagerTestV2 {
  
  @Autowired
  WaplScoreManagerV2 waplScoreV2;

  @Test
  public void getWaplScoreTypeListTest(){
		//given

		//when
		WaplScoreProbListDTOV2 typeList = waplScoreV2.getWaplScoreProbList("1-1-final", "중등-중1-1학-03-01-02", 50);

		//then
		//System.out.println(ukList);
  }
}

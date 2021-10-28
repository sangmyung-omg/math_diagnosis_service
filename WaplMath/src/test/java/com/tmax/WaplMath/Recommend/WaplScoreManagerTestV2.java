package com.tmax.WaplMath.Recommend;

import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbListDTOV2;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.util.ExamScope;
import com.tmax.WaplMath.Recommend.util.waplscore.WaplScoreManagerV2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class WaplScoreManagerTestV2 {
  
  @Autowired
  WaplScoreManagerV2 waplScoreV2;

  @Autowired
  CurriculumRepo curriculumRepo;

  @Test
  public void getWaplScoreTypeListTest(){

    waplScoreV2.setTestMode();
    
		//given
    String[] targetExamList = {"1-1-mid", "1-1-final", "1-2-mid", "1-2-final",
                               "2-1-mid", "2-1-final", "2-2-mid", "2-2-final",
                               "3-1-mid", "3-1-final", "3-2-mid", "3-2-final"};

    Integer[] remainDayList = {10, 20, 40};

		//when
    for (String targetExam:targetExamList){

      String startCurrId = ExamScope.examScope.get(targetExam).get(0);
      String endCurrId = ExamScope.examScope.get(targetExam).get(1);

      for (String subSectionId: curriculumRepo.findSubSectionListBetween(startCurrId, endCurrId)){

        for (int remainDay: remainDayList){

          log.info("{}, {}, {}", targetExam, subSectionId, remainDay);

          WaplScoreProbListDTOV2 result = waplScoreV2.getWaplScoreProbList(targetExam, subSectionId, remainDay);

		      //then
          result.getProbList().forEach(e-> {if (e.getTypeId() == null) new Exception("error"); });
        }
      }

    }

		//System.out.println(ukList);
  }
}

package com.tmax.WaplMath.Recommend;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Common.model.problem.Problem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProblemDateTest {
  
  @Autowired
  ProblemRepo probRepo;

  @Test
  public void printCurrentDate(){
    String today = ZonedDateTime.now(ZoneId.of("UTC"))
                                .minusDays(30)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    System.out.println(today);

    List<Problem> probs = probRepo.getDateProbs(today);
    
    System.out.println(probs.get(0).getCreateDate());

  }
}

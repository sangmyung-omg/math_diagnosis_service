package com.tmax.WaplMath;

import java.util.Date;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class WaplMathApplication {

  @PostConstruct
  void started() {
      TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
      log.info("Current time = {}", new Date().toString());
  }

	public static void main(String[] args) {
		SpringApplication.run(WaplMathApplication.class, args);
	}

}

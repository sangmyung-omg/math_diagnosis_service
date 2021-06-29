package com.tmax.WaplMath.Recommend.controller.mastery;

import com.tmax.WaplMath.Recommend.config.RecommendConstants;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.service.mastery.v1.MasteryServiceBaseV1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=RecommendConstants.apiPrefix + "/v1")
public class MasteryControllerV1 {
    @Autowired
	@Qualifier("MasteryServiceV1")
	private MasteryServiceBaseV1 masterySvc;

	@PutMapping(value = "/mastery")
	ResponseEntity<Object> updateMastery(@RequestHeader("token") String token) {
		ResultMessageDTO resultMessage = masterySvc.updateMasteryFromLRS(token);
		return new ResponseEntity<>(resultMessage, HttpStatus.OK);

	}
}

package com.tmax.WaplMath.AdditionalLearning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.AdditionalLearning.dto.FrequentCardDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.TodaySubsectionListDTO;
import com.tmax.WaplMath.AdditionalLearning.service.FrequentCardServiceBaseV1;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class AdditionalLearningController {
	
	@Autowired
    @Qualifier("FrequentCardServiceV1")
	private FrequentCardServiceBaseV1 FrequentCardService;

	@PutMapping(value = "/frequent", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> FrequentCard(@RequestHeader("token") String token, @RequestBody TodaySubsectionListDTO todaySubsectionList) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		FrequentCardDTO frequentCard = FrequentCardService.getFrequentCard(userId,todaySubsectionList);
		return new ResponseEntity<>(frequentCard, HttpStatus.OK);
	}
}

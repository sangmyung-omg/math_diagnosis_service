package com.tmax.WaplMath.AdditionalLearning.controller.frequent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.AdditionalLearning.config.AddLearnConstants;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentCardDTO;
import com.tmax.WaplMath.AdditionalLearning.service.frequentcard.FrequentCardServiceBaseV2;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=AddLearnConstants.apiPrefix + "/v2")
public class FrequentCardControllerV2 {
	@Autowired
    @Qualifier("FrequentCardServiceV2")
	private FrequentCardServiceBaseV2 FrequentCardService;

	@GetMapping(value = "/frequent", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> FrequentCard(@RequestHeader("token") String token, @RequestParam("isFirstFrequent") boolean isFirstFrequent) {
		String userId = JWTUtil.getUserID(token);
		FrequentCardDTO frequentCard = FrequentCardService.getFrequentCard(userId,isFirstFrequent);
		return new ResponseEntity<>(frequentCard, HttpStatus.OK);
	}
}
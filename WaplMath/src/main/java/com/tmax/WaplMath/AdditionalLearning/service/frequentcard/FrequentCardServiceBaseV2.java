package com.tmax.WaplMath.AdditionalLearning.service.frequentcard;

import com.tmax.WaplMath.AdditionalLearning.dto.FrequentCardDTO;

public interface FrequentCardServiceBaseV2 {
	
	FrequentCardDTO getFrequentCard(String userId, boolean isFirstFrequent);

}

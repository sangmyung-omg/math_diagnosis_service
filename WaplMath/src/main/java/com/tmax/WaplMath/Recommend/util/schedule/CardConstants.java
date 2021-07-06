package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.HashMap;
import java.util.Map;

public class CardConstants {
	
	// Hyperparameter
	static final Integer MAX_CARD_PROB_NUM = 20;
	
	static final Integer MAX_TYPE_CARD_NUM = 4;
	static final Integer MIN_TYPE_CARD_PROB_NUM = 2;
	static final Integer MAX_TYPE_CARD_PROB_NUM = 5;

	static final Integer SUPPLE_CARD_TYPE_NUM = 3;
	static final Integer SUPPLE_CARD_PROB_NUM_PER_TYPE = 2;
	static final Integer SUPPLE_CARD_TYPE_THRESHOLD = 30;
	
	static final Integer SECTION_TEST_CARD_HIGH_PROB = 5;
	static final Integer SECTION_TEST_MIDDLE_PROB = 9;
	static final Integer SECTION_TEST_LOW_PROB = 6;
	
	static final float MASTERY_HIGH_THRESHOLD = 0.7f;
	static final float MASTERY_LOW_THRESHOLD = 0.4f;

	static final Integer AVERAGE_PROB_ESTIMATED_TIME = 180;
	
	// Constant
	static final String TYPE_CARD_TYPESTR = "type";
	static final String SUPPLE_CARD_TYPESTR = "supple";
	static final String SECTION_TEST_CARD_TYPESTR = "section_test";
	static final String CHAPTER_TEST_CARD_TYPESTR = "chapter_test ";
	static final String ADDTL_SUPPLE_CARD_TYPESTR = "addtl_supple";
	static final String SECTION_EXAM_CARD_TYPESTR = "section_exam";
	static final String FULL_SCOPE_EXAM_CARD_TYPESTR = "full_scope_exam";
	static final String TRIAL_EXAM_CARD_TYPESTR = "trial_exam";
	
	static final String SUPPLE_CARD_TITLE_FORMAT = "취약 유형 %d개 복습";
	static final String ADDTL_SUPPLE_CARD_TITLE_FORMAT = "유형 %d개 복습";
	static final String TRIAL_EXAM_CARD_TITLE_FORMAT = "중학교 %s학년 %s학기 %s 대비";
	
	static Map<String, String> diffKorEngMap = new HashMap<String, String>();	
	static {
		diffKorEngMap.put("상", "high");
		diffKorEngMap.put("중", "middle");
		diffKorEngMap.put("하", "low");
	}
}

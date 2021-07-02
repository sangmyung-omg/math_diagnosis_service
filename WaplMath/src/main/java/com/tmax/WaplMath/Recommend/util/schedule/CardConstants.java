package com.tmax.WaplMath.Recommend.util.schedule;


public class CardConstants {
	
	// Hyperparameter
	static final Integer MIN_TYPE_CARD_PROB_NUM = 2;
	static final Integer MAX_TYPE_CARD_PROB_NUM = 5;
	static final Integer SUPPLE_CARD_PROB_NUM_PER_TYPE = 2;
	static final Integer MID_EXAM_CARD_PROB_NUM = 20;
	static final Integer MID_EXAM_CARD_HIGH_PROB = 5;
	static final Integer MID_EXAM_CARD_MIDDLE_PROB = 9;
	static final Integer MID_EXAM_CARD_LOW_PROB = 6;
	static final Integer SUPPLE_CARD_TYPE_NUM = 3;
	static final Integer MAX_CARD_PROB_NUM = 20;
	static final Integer SUPPLE_CARD_TYPE_THRESHOLD = 30;
	static final Float LOW_MASTERY_THRESHOLD = 1.0f;

	static final Integer AVERAGE_PROB_ESTIMATED_TIME = 180;
	static final float MASTERY_HIGH_THRESHOLD = 0.7f;
	static final float MASTERY_LOW_THRESHOLD = 0.4f;

	// Constant
	static final String TYPE_CARD_TYPE = "type";
	static final String SUPPLE_CARD_TYPE = "supple";
	static final String ADDTL_SUPPLE_CARD_TYPE = "addtlSupple";
	static final String SECTION_MID_EXAM_CARD_TYPE = "sectionMidExam";
	static final String CHAPTER_MID_EXAM_CARD_TYPE = "chapterMidExam";
	static final String TRIAL_EXAM_CARD_TYPE = "trialExam";
	static final String SUPPLE_CARD_TITLE_FORMAT = "취약 유형 %d개 복습";
	static final String ADDTL_SUPPLE_CARD_TITLE_FORMAT = "유형 %d개 복습";
	static final String TRIAL_EXAM_CARD_TITLE_FORMAT = "중학교 %s학년 %s학기 %s 대비";
}

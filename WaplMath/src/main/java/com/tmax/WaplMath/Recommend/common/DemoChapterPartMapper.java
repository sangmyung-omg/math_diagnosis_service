package com.tmax.WaplMath.Recommend.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoChapterPartMapper {
	
	public Map<String, List<String>> getMappingInfo(){
		Map<String, List<String>> chapterIdList = new HashMap<String, List<String>>();
		List<String> temp = new ArrayList<String>();
		temp.add("중등-중1-1학-01"); //temp.add("수와 연산");
		temp.add("중등-중2-1학-01"); //temp.add("유리수와 소수");
		temp.add("중등-중3-1학-01"); //temp.add("제곱근과 실수");
		chapterIdList.put("0", temp);
		
		temp = new ArrayList<String>();
		temp.add("중등-중1-1학-02"); //temp.add("문자와 식");
		temp.add("중등-중2-1학-02"); //temp.add("식의 계산");
		temp.add("중등-중2-1학-03"); //temp.add("일차부등식과 연립일차방정식");
		temp.add("중등-중3-1학-02"); //temp.add("다항식의 곱셈과 인수분해");
		temp.add("중등-중3-1학-03"); //temp.add("이차방정식");
		chapterIdList.put("1", temp);
		
		temp = new ArrayList<String>();
		temp.add("중등-중1-1학-03"); //temp.add("좌표평면과 그래프");
		temp.add("중등-중2-1학-04"); //temp.add("함수");
		temp.add("중등-중3-1학-04"); //temp.add("이차함수");
		chapterIdList.put("2", temp);
		
		temp = new ArrayList<String>();
		temp.add("중등-중1-2학-01"); //temp.add("기본 도형");
		temp.add("중등-중1-2학-02"); //temp.add("평면도형과 입체도형");
		temp.add("중등-중2-2학-01"); //temp.add("도형의 성질");
		temp.add("중등-중2-2학-02"); //temp.add("도형의 닮음");
		temp.add("중등-중2-2학-03"); //temp.add("피타고라스의 정리");
		temp.add("중등-중3-2학-01"); //temp.add("삼각비");
		temp.add("중등-중3-2학-02"); //temp.add("원의 성질");
		chapterIdList.put("3", temp);
		
		temp = new ArrayList<String>();
		temp.add("중등-중1-2학-03"); //temp.add("통계");
		temp.add("중등-중2-2학-04"); //temp.add("확률");
		temp.add("중등-중3-2학-03"); //temp.add("통계");
		chapterIdList.put("4", temp);
		return chapterIdList;
	}
}

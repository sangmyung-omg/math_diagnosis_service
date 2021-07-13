package com.tmax.WaplMath.Recommend.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default exam scope in sub-sections by grade/semester/exam type
 * @author Sangheon_lee
 */
public class ExamScope {
	public static final Map<String, List<String>> examScope = new HashMap<String, List<String>>();
	
	static {
		examScope.put("1-1-mid", new ArrayList<String>(Arrays.asList("중등-중1-1학-01-01-01", "중등-중1-1학-02-01-03")));
		examScope.put("1-1-final", new ArrayList<String>(Arrays.asList("중등-중1-1학-02-01-01", "중등-중1-1학-03-02-02")));
		examScope.put("1-2-mid", new ArrayList<String>(Arrays.asList("중등-중1-2학-01-01-01", "중등-중1-2학-02-02-02")));
		examScope.put("1-2-final", new ArrayList<String>(Arrays.asList("중등-중1-2학-02-02-01", "중등-중1-2학-04-01-03")));
		
		examScope.put("2-1-mid", new ArrayList<String>(Arrays.asList("중등-중2-1학-01-01-01", "중등-중2-1학-02-02-01")));
		examScope.put("2-1-final", new ArrayList<String>(Arrays.asList("중등-중2-1학-02-03-01", "중등-중2-1학-03-02-02")));
		examScope.put("2-2-mid", new ArrayList<String>(Arrays.asList("중등-중2-2학-01-01-01", "중등-중2-2학-03-01-02")));
		examScope.put("2-2-final", new ArrayList<String>(Arrays.asList("중등-중2-2학-03-02-01", "중등-중2-2학-05-02-02")));
		
		examScope.put("3-1-mid", new ArrayList<String>(Arrays.asList("중등-중3-1학-01-01-01", "중등-중3-1학-03-01-04")));
		examScope.put("3-1-final", new ArrayList<String>(Arrays.asList("중등-중3-1학-03-01-01", "중등-중3-1학-04-01-04")));
		examScope.put("3-2-mid", new ArrayList<String>(Arrays.asList("중등-중3-2학-01-01-01", "중등-중3-2학-02-02-02")));
		examScope.put("3-2-final", new ArrayList<String>(Arrays.asList("중등-중3-2학-02-02-01", "중등-중3-2학-03-02-01")));
	}
}

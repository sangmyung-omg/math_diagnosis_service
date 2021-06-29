package com.tmax.WaplMath.Recommend.service.mastery;

import java.util.List;

import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;

public interface MasteryServiceBase {
	ResultMessageDTO updateMastery(String userId, List<String> probIdList, List<String> correctList);
}

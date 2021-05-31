package com.tmax.WaplMath.Recommend.service.mastery;

import java.util.List;

import com.tmax.WaplMath.Recommend.model.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.model.ResultMessageDTO;

public interface MasteryServiceBase {
	ResultMessageDTO updateMastery(String userId, List<String> probIdList, List<String> correctList);
}

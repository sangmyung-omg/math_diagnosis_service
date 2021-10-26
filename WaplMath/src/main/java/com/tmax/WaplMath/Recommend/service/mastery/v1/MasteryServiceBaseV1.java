package com.tmax.WaplMath.Recommend.service.mastery.v1;

import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.service.mastery.MasteryServiceBase;

/**
 * Version 1 extension of mastery service base
 * @author Jonghyun Seong
 */
public interface MasteryServiceBaseV1 extends MasteryServiceBase {
    @Deprecated
    public ResultMessageDTO updateMasteryFromLRS(String token);

    public ResultMessageDTO updateMasteryWithLRS(String userID);
}

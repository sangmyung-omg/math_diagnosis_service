package com.tmax.WaplMath.Recommend.service.userinfo;

import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.user.UserScheduleScopeInDTO;
import com.tmax.WaplMath.Recommend.dto.user.UserScheduleScopeOutDTO;

public interface UserScheduleScopeBase {
  UserScheduleScopeOutDTO getScheduleScope(String userId);
  ResultMessageDTO updateScheduleScope(String userId, UserScheduleScopeInDTO input);
}

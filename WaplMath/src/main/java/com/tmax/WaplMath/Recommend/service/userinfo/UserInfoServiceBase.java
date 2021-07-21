package com.tmax.WaplMath.Recommend.service.userinfo;

import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.UserBasicInfoDTO;
import com.tmax.WaplMath.Recommend.dto.UserExamInfoDTO;

public interface UserInfoServiceBase {
	User getUserInfo(String userId);
	ResultMessageDTO updateExamInfo(String userId, UserExamInfoDTO input);
	ResultMessageDTO updateBasicInfo(String userId, UserBasicInfoDTO input);
	ResultMessageDTO deleteUserInfo(String userId);
}

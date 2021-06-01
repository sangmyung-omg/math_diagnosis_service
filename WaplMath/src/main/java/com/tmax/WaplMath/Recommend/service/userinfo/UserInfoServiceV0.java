package com.tmax.WaplMath.Recommend.service.userinfo;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.UserRepository;

@Service
@Qualifier("UserInfoServiceV0")
public class UserInfoServiceV0 implements UserInfoBase {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public User getUserInfo(String userId) {
		User result = new User();
		List<String> input = new ArrayList<String>();
		input.add(userId);
		logger.info("Getting user basic info...");
		List<User> queryList = (List<User>) userRepository.findAllById(input);
		logger.info("user : " + input + ", Query Result Size: " + Integer.toString(queryList.size()));
		if (queryList.size() != 0 && queryList != null) {
			result = queryList.get(0);			
		}
		return result;
	}
}

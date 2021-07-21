package com.tmax.WaplMath.AdditionalLearning.util.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.tmax.WaplMath.AdditionalLearning.repository.UserExamScopeRepo;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.model.user.UserExamScope;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component("AddLearn-UserExamScope")
public class UserExamScopeUtil{

	@Autowired
	@Qualifier("AddLearn-UserExamScopeRepo")
	UserExamScopeRepo userExamScopeRepo;
	
	public List<String> getUserExamScope(String userId){
		List<String> scope = new ArrayList<String>();
		UserExamScope examScope = userExamScopeRepo.getExamScopeOfUser(userId);
		if(examScope!=null) {
			scope.add(examScope.getStartSubSectionId());
			scope.add(examScope.getEndSubSectionId());
		}else {
			throw new GenericInternalException("ERR-AL-003","Exam scope data is not found in DB. User ID = "+userId);
		}
		return scope;
	}
}

package com.tmax.WaplMath.DBUpdate.service.problem;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tmax.WaplMath.DBUpdate.repository.problem.DBUpdateProblemJpaRepository;
import com.tmax.WaplMath.DBUpdate.repository.problem.DBUpdateProblemRepository;
import com.tmax.WaplMath.Recommend.model.problem.Problem;

import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class ProblemService {
	
	private final DBUpdateProblemJpaRepository problemJpaRepository;
	
	private final DBUpdateProblemRepository problemRepository;
	
	
	/**
	 * 문제 조회
	 */
	//id 로 조회
	public Problem  findOne(Long probId) {
//		return problemRepository.findByProbId(probId);
		return problemJpaRepository.findOne(probId.intValue());
	}
	
	//특정 시간 이후 Accept 으로 업데이트 된 문제 조회
		public List<Problem>  findAcceptedProbIdsAfterInputTime(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
			Timestamp timestamp = Timestamp.valueOf(dateTime);
			return problemRepository.findByValidateDateGreaterThanAndStatusIs(timestamp,"ACCEPT");
		}
		
	//특정 시간 이후 else Accept 으로 업데이트 된 문제 조회
	public List<Problem>  findElseProbIdsAfterInputTime(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
		Timestamp timestamp = Timestamp.valueOf(dateTime);
		return problemRepository.findByValidateDateGreaterThanAndStatusNot(timestamp,"ACCEPT");
	}
	
	
	
	
}

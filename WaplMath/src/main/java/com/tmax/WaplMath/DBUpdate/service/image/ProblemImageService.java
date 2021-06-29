package com.tmax.WaplMath.DBUpdate.service.image;


import java.util.List;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tmax.WaplMath.DBUpdate.repository.image.ProblemImageJpaRepository;
import com.tmax.WaplMath.Recommend.model.problem.ProblemImage;

import lombok.RequiredArgsConstructor;



@Service
@Transactional
@RequiredArgsConstructor
public class ProblemImageService {
	
	private final ProblemImageJpaRepository imageJpaRepository;

	
	/**
	 * 조회
	 */
	//probId 로 조회
	public List<ProblemImage> findByProbId(Long probId) {
		
		return imageJpaRepository.findByProbId(probId.intValue());

	}
	
	
	
}

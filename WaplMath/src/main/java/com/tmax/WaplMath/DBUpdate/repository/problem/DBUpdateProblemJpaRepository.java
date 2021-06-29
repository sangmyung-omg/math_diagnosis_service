package com.tmax.WaplMath.DBUpdate.repository.problem;



import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.Recommend.model.problem.Problem;

@Repository
public class DBUpdateProblemJpaRepository 
{
	
	@PersistenceContext
	private EntityManager em;
	
	public Problem findOne(Integer id) {
		return em.find(Problem.class, id);
	}
	
	
}

package com.tmax.WaplMath.DBUpdate.repository.image;

import java.util.List;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.Recommend.model.problem.ProblemImage;




@Repository
public class ProblemImageJpaRepository 
{
	
	@PersistenceContext
	private EntityManager em;  
	
	public List<ProblemImage> findByProbId(Integer probId) {
		 return em.createQuery("select m from ProblemImage m where m.problem.probId = :probId",
		ProblemImage.class)
		 .setParameter("probId", probId)
		 .getResultList();
		 }
	
}

package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import java.util.Set;
import com.tmax.WaplMath.Common.model.problem.Problem;
import com.tmax.WaplMath.Common.model.problem.ProblemUkRel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("RE-ProblemUkRelRepo")
public interface ProblemUkRelRepo extends CrudRepository<ProblemUkRel, Integer> {

	@Query("select ukId from ProblemUkRel PUKR where PUKR.probId = :probId")
	public List<Integer> findAllUkIdByProbId(@Param("probId") Integer probId);

	//2021-06-17 Added by Jonghyun Seong for whole ProbUKGet
	@Query("select pukr from ProblemUkRel pukr where pukr.probId in :probIdList")
	public List<ProblemUkRel> getByProblemIDList(@Param("probIdList") List<Integer> probIdList);
}

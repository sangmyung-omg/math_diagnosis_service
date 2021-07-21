package com.tmax.WaplMath.Common.repository.problem;

import com.tmax.WaplMath.Common.model.problem.Problem;
import org.springframework.data.repository.CrudRepository;

public interface ProblemRepo extends CrudRepository<Problem, Integer> {
  
}

package com.tmax.WaplMath.Common.repository.problem;

import com.tmax.WaplMath.Common.model.problem.ProblemImage;
import com.tmax.WaplMath.Common.model.problem.ProblemImageKey;
import org.springframework.data.repository.CrudRepository;

public interface ProblemImageRepo extends CrudRepository<ProblemImage, ProblemImageKey> {
  
}

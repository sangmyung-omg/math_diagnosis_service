package com.tmax.WaplMath.Common.repository.problem;

import java.util.List;

import com.tmax.WaplMath.Common.model.problem.ProblemType;
import org.springframework.data.repository.CrudRepository;

public interface ProblemTypeRepo extends CrudRepository<ProblemType, Integer> {
  List<ProblemType> findByTypeId(Integer typeId);
  List<ProblemType> findByCurriculumIdStartsWith(String curriculumId);
  List<ProblemType> findByCurriculumId(String curriculumId);
}

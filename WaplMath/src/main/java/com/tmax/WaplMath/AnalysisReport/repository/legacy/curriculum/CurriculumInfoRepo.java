package com.tmax.WaplMath.AnalysisReport.repository.legacy.curriculum;

import java.util.List;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CurriculumInfoRepo extends CrudRepository<Curriculum, String>{

    @Query(value="select * from curriculum_master where curriculum_id like :currRange order by curriculum_sequence", nativeQuery = true)
    List<Curriculum> getCurriculumListByRange(@Param("currRange") String currRange);

    @Query(value="select * from curriculum_master where curriculum_id like :currRange and section is not null order by curriculum_sequence", nativeQuery = true)
    List<Curriculum> getCurriculumListByRangeSectionOnly(@Param("currRange") String currRange);
    
}

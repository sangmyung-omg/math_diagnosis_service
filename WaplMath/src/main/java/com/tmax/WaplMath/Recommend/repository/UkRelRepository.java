package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.uk.UkRel;
import com.tmax.WaplMath.Recommend.model.uk.UkRelKey;

public interface UkRelRepository extends CrudRepository<UkRel, UkRelKey> {

	@Query("select distinct UR.preUkId from UkRel UR where UR.baseUkId in (:baseUkIdList) and UR.relationReference=:relationReference")
	List<Integer> findPreUkIdList(@Param("baseUkIdList") List<Integer> baseUkIdList,
			@Param("relationReference") String relationReference);

}

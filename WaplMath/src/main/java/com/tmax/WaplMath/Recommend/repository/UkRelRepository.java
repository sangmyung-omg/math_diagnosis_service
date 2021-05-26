package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.UkRel;
import com.tmax.WaplMath.Recommend.model.UkRelKey;

public interface UkRelRepository extends CrudRepository<UkRel, UkRelKey> {
	
	@Query(value="select distinct UR.pre_uk_uuid from uk_rel UR "
			+ "where UR.base_uk_uuid in ?1 "
			+ "and UR.relation_reference=?2", nativeQuery=true)
	List<String> findPreUkUuidList(List<String> baseUkId, String relationReference);

}

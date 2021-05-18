package com.tmax.Recommend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UkRelRepository extends CrudRepository<UkRelDAO, UkRelKey> {
	
	@Query(value="select distinct UR.pre_uk_uuid from uk_rel UR "
			+ "where UR.base_uk_uuid in ?1 "
			+ "and UR.relation_reference=?2", nativeQuery=true)
	List<String> findPreUkUuidList(List<String> baseUkUuid, String relationReference);

}

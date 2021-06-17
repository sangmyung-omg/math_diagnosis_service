package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.uk.TypeUkRel;
import com.tmax.WaplMath.Recommend.model.uk.TypeUkRelKey;

public interface TypeUkRelRepo extends CrudRepository<TypeUkRel, TypeUkRelKey> {
	@Query("select tur.ukId from TypeUkRel tur where tur.typeId=:typeId")
	public List<Integer> findAllUkByTypeId(@Param("typeId") Integer typeId);
}

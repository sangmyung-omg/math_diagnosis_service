package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import com.tmax.WaplMath.Common.model.uk.TypeUkRel;
import com.tmax.WaplMath.Common.model.uk.TypeUkRelKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("RE-TypeUkRelRepo")
public interface TypeUkRelRepo extends CrudRepository<TypeUkRel, TypeUkRelKey> {
	@Query("select tur.ukId from TypeUkRel tur where tur.typeId=:typeId")
	public List<Integer> findAllUkByTypeId(@Param("typeId") Integer typeId);
}

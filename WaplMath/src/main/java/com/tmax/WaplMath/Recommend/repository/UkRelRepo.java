package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import com.tmax.WaplMath.Common.model.uk.UkRel;
import com.tmax.WaplMath.Common.model.uk.UkRelKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("RE-UkRelRepo")
public interface UkRelRepo extends CrudRepository<UkRel, UkRelKey> {

}

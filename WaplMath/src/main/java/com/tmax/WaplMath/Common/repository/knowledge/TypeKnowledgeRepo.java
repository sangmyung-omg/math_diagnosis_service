package com.tmax.WaplMath.Common.repository.knowledge;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledgeKey;

public interface TypeKnowledgeRepo extends CrudRepository<TypeKnowledge, TypeKnowledgeKey> {
    List<TypeKnowledge> findByTypeId(Integer typeId);
    List<TypeKnowledge> findByTypeIdIn(List<Integer> typeIdList);
}

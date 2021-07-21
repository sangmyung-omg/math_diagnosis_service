package com.tmax.WaplMath.Common.repository.knowledge;

import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledgeKey;
import org.springframework.data.repository.CrudRepository;

public interface UserKnowledgeRepo extends CrudRepository<UserKnowledge, UserKnowledgeKey> {
  
}

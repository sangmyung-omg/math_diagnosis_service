package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.UserKnowledgeKey;

public interface UserKnowledgeRepository extends CrudRepository<UserKnowledge, UserKnowledgeKey> {
	
//	@Query("SELECT ukd FROM UserKnowledgeDAO ukd INNER JOIN UkDAO ud ON ukd.ukUuid = ud.ukUuid WHERE ukd.userUuid = ?1 AND SUBSTR(ud.curriculumId, 1, 11) = ?2 ORDER BY ud.curriculumId asc")
	@Query("SELECT ukd FROM UserKnowledgeDAO ukd WHERE ukd.userUuid = ?1 AND SUBSTR(ukd.ukDao.curriculumId, 1, 11) = ?2 ORDER BY ukd.ukDao.curriculumId asc")
	List<UserKnowledge> findAllByUserAndChapter(String user_uuid, String curriculum_id);
}

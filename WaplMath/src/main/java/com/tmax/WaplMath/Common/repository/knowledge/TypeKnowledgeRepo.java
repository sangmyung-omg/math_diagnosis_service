package com.tmax.WaplMath.Common.repository.knowledge;

import java.util.List;
import java.util.Set;
import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledgeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TypeKnowledgeRepo extends CrudRepository<TypeKnowledge, TypeKnowledgeKey> {
    List<TypeKnowledge> findByTypeId(Integer typeId);
    List<TypeKnowledge> findByTypeIdIn(List<Integer> typeIdList);



    List<TypeKnowledge> findByUserUuid(String userUuid);

    @Query("Select type from TypeKnowledge type where type.userUuid=:userUuid and type.typeId in :typeIds")
    List<TypeKnowledge> findByUserUuidAndTypeIds(@Param("userUuid") String userUuid,@Param("typeIds") List<Integer> typeIds);

    @Query("Select type from TypeKnowledge type where type.userUuid = :userUuid and type.typeId in (:candidateTypes) order by type.typeMastery desc")
    List<TypeKnowledge> findByUserIDSortedDesc(@Param("userUuid") String userUuid, @Param("candidateTypes") Set<Integer> candidateTypes);

    @Query(value="select * from (Select * from TYPE_KNOWLEDGE type where type.user_uuid = :userUuid and type.type_id in (:candidateTypes) order by type.type_mastery desc) where rownum <= :limit", nativeQuery = true)
    List<TypeKnowledge> findByUserIDSortedLimitedDesc(@Param("userUuid") String userUuid, @Param("candidateTypes") Set<Integer> candidateTypes, @Param("limit")  Integer limit);

    @Query("Select type from TypeKnowledge type where type.userUuid = :userUuid and type.typeId in (:candidateTypes) order by type.typeMastery asc")
    List<TypeKnowledge> findByUserIDSortedAsc(@Param("userUuid") String userUuid, @Param("candidateTypes") Set<Integer> candidateTypes);

    @Query(value="select * from (Select * from TYPE_KNOWLEDGE type where type.user_uuid = :userUuid and type.type_id in (:candidateTypes) order by type.type_mastery asc) where rownum <= :limit", nativeQuery = true)
    List<TypeKnowledge> findByUserIDSortedLimitedAsc(@Param("userUuid") String userUuid, @Param("candidateTypes") Set<Integer> candidateTypes, @Param("limit")  Integer limit);
}

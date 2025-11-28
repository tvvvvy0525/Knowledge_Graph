package com.pinewilt.kg.repository;

import com.pinewilt.kg.dto.DynamicRelationResult; // 记得导入刚才建的 DTO
import com.pinewilt.kg.model.EntityNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface EntityNodeRepository extends Neo4jRepository<EntityNode, Long> {

    // 1. 你的旧方法 (可以直接用)
    @Query("MATCH (n:Entity) RETURN n LIMIT 25")
    List<EntityNode> findAllNodes();

    @Query("MATCH (n:Entity) WHERE n.category = $category RETURN n")
    List<EntityNode> findByCategory(@Param("category") String category);

    @Query("MATCH (n:Entity) WHERE n.id = $id RETURN n")
    Optional<EntityNode> findOneByBusinessId(@Param("id") Long id);

    @Query("MATCH (n:Entity) WHERE toLower(n.name) CONTAINS toLower($keyword) OR n.cn_name CONTAINS $keyword RETURN n")
    List<EntityNode> search(@Param("keyword") String keyword);

    @Query("MATCH (start:Entity {id: $id})-[r]->(end:Entity) " +
            "RETURN type(r) AS relType, " +
            "       r.cn_name AS relCnName, " +
            "       end AS target")
    List<DynamicRelationResult> findAllRelationsByEntityId(@Param("id") Long id);

}
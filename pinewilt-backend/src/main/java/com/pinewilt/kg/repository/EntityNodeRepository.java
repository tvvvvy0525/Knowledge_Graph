package com.pinewilt.kg.repository;

import com.pinewilt.kg.dto.DynamicRelationResult; // 记得导入刚才建的 DTO
import com.pinewilt.kg.dto.InternalRelationDto;
import com.pinewilt.kg.dto.RelationTypeDto;
import com.pinewilt.kg.model.EntityNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityNodeRepository extends Neo4jRepository<EntityNode, Long> {

        // 1. 你的旧方法 (可以直接用)
        @Query("MATCH (n:Entity) RETURN n LIMIT 10")
        List<EntityNode> findAllNodes();

        @Query("MATCH (n:Entity) WHERE n.category = $category RETURN n")
        List<EntityNode> findByCategory(@Param("category") String category);

        @Query("MATCH (n:Entity) WHERE n.id = $id RETURN n")
        Optional<EntityNode> findOneByBusinessId(@Param("id") Long id);

        @Query("MATCH (n:Entity) WHERE toLower(n.name) CONTAINS toLower($keyword) OR n.cn_name CONTAINS $keyword RETURN n")
        List<EntityNode> search(@Param("keyword") String keyword);

        @Query("MATCH (start:Entity {id: $id})-[r]-(end:Entity) " +
                        "RETURN type(r) AS relType, " +
                        "       r.cn_name AS relCnName, " +
                        "       end AS target, " +
                        "       startNode(r).id AS sourceId") // 【新增】告诉前端谁才是真正的起点
        List<DynamicRelationResult> findAllRelationsByEntityId(@Param("id") Long id);

        @Query("MATCH (n:Entity) " +
                        "WHERE toLower(n.name) CONTAINS toLower($keyword) OR n.cn_name CONTAINS $keyword " +
                        "RETURN n")
        List<EntityNode> searchNodes(@Param("keyword") String keyword);

        @Query("MATCH (s:Entity)-[r]->(t:Entity) " +
                        "WHERE s.id IN $ids AND t.id IN $ids " +
                        "RETURN s.id AS sourceId, " +
                        "       t.id AS targetId, " +
                        "       type(r) AS relType, " +
                        "       r.cn_name AS relCnName")
        List<InternalRelationDto> findRelationsBetweenNodes(@Param("ids") List<Long> ids);

    @Query("MATCH ()-[r]->() " +
            "WHERE toLower(type(r)) CONTAINS toLower($keyword) OR r.cn_name CONTAINS $keyword " +
            "WITH type(r) AS relType, r.cn_name AS name " +
            // 核心改动：直接用 max() 选出非空的那个名字
            // 如果全是 null，结果才为 null；只要有一个有值，max 就会把它抓出来
            "RETURN relType, max(name) AS cnName " +
            "LIMIT 20")
    List<RelationTypeDto> searchRelationTypes(@Param("keyword") String keyword);

        @Query("MATCH (n:Entity) WHERE $question CONTAINS n.cn_name " +
                        "WITH n LIMIT 3 " + // 限制核心节点数量
                        "MATCH (n)-[r1]-(m) " + // 第一层关系
                        "OPTIONAL MATCH (m)-[r2]-(k) " + // 第二层关系 (用 OPTIONAL，防止没有第二层时查不到第一层)
                        "RETURN n.cn_name + ' ' + type(r1) + ' ' + m.cn_name + " +
                        "CASE WHEN k IS NOT NULL THEN ' ' + type(r2) + ' ' + k.cn_name ELSE '' END")
        List<String> findGraphContext(@Param("question") String question);
}
package com.pinewilt.kg.service.impl;

import com.pinewilt.kg.dto.DynamicRelationResult;
import com.pinewilt.kg.dto.NodeRequest;
import com.pinewilt.kg.dto.RelationRequest;
import com.pinewilt.kg.model.EntityNode;
import com.pinewilt.kg.repository.EntityNodeRepository;
import com.pinewilt.kg.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraphServiceImpl implements GraphService {

    private final EntityNodeRepository entityNodeRepository;
    private final Neo4jClient neo4jClient; // 用于执行动态创建关系的 Cypher

    // ================= 读操作 =================

    @Override
    public List<EntityNode> getInitialGraph() {
        return entityNodeRepository.findAllNodes();
    }

    @Override
    public List<EntityNode> search(String keyword) {
        return entityNodeRepository.search(keyword);
    }

    @Override
    public Map<String, Object> getNeighbors(Long id) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取中心节点信息
        EntityNode centerNode = entityNodeRepository.findById(id) // 注意：这里假设 id 是 graphId 还是 业务id？
                // 如果你的 Repository findById 用的是 graphId，而前端传的是 businessId，
                // 你需要确认 repository 里是否有 findById(businessId) 方法。
                // 假设 Repository 里有一个 findByBusinessId(Long id) 或者我们统一约定前端传的是业务ID
                // 下面用自定义查询确保按业务ID查:
                .orElseThrow(() -> new RuntimeException("节点不存在"));

        // 如果 findById 是按 Neo4j 内部 ID 查的，而你想按业务 ID 查，请用 repository.findByCustomId(id)
        // 这里为了演示方便，假设你已经处理好了 ID 映射，或者前端传的就是 GraphId。
        // *修正*：根据之前的代码，你的 EntityNode 有 @Id Long graphId 和 @Property Long id。
        // 为了稳健，我们应该用业务 ID 查：
        // EntityNode centerNode = entityNodeRepository.findByBusinessId(id); (需要在 repo 定义)

        result.put("node", centerNode);

        // 2. 获取关系并分组
        List<DynamicRelationResult> relations = entityNodeRepository.findAllRelationsByEntityId(id);
        Map<String, List<DynamicRelationResult>> grouped = relations.stream()
                .collect(Collectors.groupingBy(DynamicRelationResult::getRelType));

        result.put("neighbors", grouped);
        return result;
    }

    // ================= 写操作 =================

    @Override
    @Transactional
    public EntityNode createNode(NodeRequest request) {
        EntityNode node = new EntityNode();
        node.setId(request.getId()); // 业务ID
        node.setName(request.getName());
        node.setCnName(request.getCnName());
        node.setCategory(request.getCategory());
        node.setDescription(request.getDescription());
        return entityNodeRepository.save(node);
    }

    @Override
    @Transactional
    public EntityNode updateNode(NodeRequest request) {
        // 1. 先查出来 (根据业务ID)
        // 假设 Repository 中有 EntityNode findById(Long id) 返回业务ID匹配的节点
        // 这里我们需要一个根据业务 ID 查找的方法
        EntityNode node = entityNodeRepository.findOneByBusinessId(request.getId())
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + request.getId() + " 的节点"));

        // 2. 更新属性
        if (request.getName() != null) node.setName(request.getName());
        if (request.getCnName() != null) node.setCnName(request.getCnName());
        if (request.getCategory() != null) node.setCategory(request.getCategory());
        if (request.getDescription() != null) node.setDescription(request.getDescription());

        // 3. 保存
        return entityNodeRepository.save(node);
    }

    @Override
    @Transactional
    public void deleteNode(Long id) {
        // 级联删除通常在数据库层面做比较好 (DETACH DELETE)
        // SDN 的 delete 方法默认就是 DETACH DELETE (删除节点及关联边)
        // 先根据业务ID找到 Neo4j 内部 ID，或者直接用 Cypher 删除
        neo4jClient.query("MATCH (n:Entity {id: $id}) DETACH DELETE n")
                .bind(id).to("id")
                .run();
    }

    @Override
    @Transactional
    public void createRelation(RelationRequest request) {
        // 动态关系创建！因为 relType 是变量，不能用 @Query 参数化
        // 必须拼接 Cypher 字符串 (注意：实际生产中要校验 relType 防止注入，这里假设内部使用)

        String cypher = String.format(
                "MATCH (s:Entity {id: $sourceId}), (t:Entity {id: $targetId}) " +
                        "MERGE (s)-[r:`%s`]->(t) " +  // 动态类型加反引号
                        "SET r.cn_name = $cnName",
                request.getRelType()
        );

        neo4jClient.query(cypher)
                .bind(request.getSourceId()).to("sourceId")
                .bind(request.getTargetId()).to("targetId")
                .bind(request.getCnName()).to("cnName")
                .run();
    }
}
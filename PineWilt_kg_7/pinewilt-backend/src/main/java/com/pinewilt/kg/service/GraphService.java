package com.pinewilt.kg.service;

import com.pinewilt.kg.dto.DynamicRelationResult;
import com.pinewilt.kg.dto.NodeRequest;
import com.pinewilt.kg.dto.RelationRequest;
import com.pinewilt.kg.model.EntityNode;

import java.util.List;
import java.util.Map;

public interface GraphService {
    // === 读操作 ===
    List<EntityNode> getInitialGraph();
    Map<String, Object> getNeighbors(Long id); // 返回分组后的邻居信息
    List<EntityNode> search(String keyword);

    // === 写操作 (Manage) ===
    EntityNode createNode(NodeRequest request);
    EntityNode updateNode(NodeRequest request);
    void deleteNode(Long id);
    void createRelation(RelationRequest request);
}
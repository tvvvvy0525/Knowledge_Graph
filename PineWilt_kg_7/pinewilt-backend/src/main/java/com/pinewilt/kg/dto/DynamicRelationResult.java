package com.pinewilt.kg.dto;

import com.pinewilt.kg.model.EntityNode;

// 定义这个接口，用来接收 "类型 + 中文名 + 目标节点"
public interface DynamicRelationResult {

    // 对应 Cypher 里的: RETURN type(r) AS relType
    String getRelType();

    // 对应 Cypher 里的: RETURN r.cn_name AS relCnName
    String getRelCnName();

    // 对应 Cypher 里的: RETURN end AS target
    EntityNode getTarget();
}
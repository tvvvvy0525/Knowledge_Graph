package com.pinewilt.kg.dto;

import com.pinewilt.kg.model.EntityNode;
import lombok.Data;

@Data
public class DynamicRelationResult {

    // 对应 SQL: RETURN type(r) AS relType
    private String relType;

    // 对应 SQL: RETURN r.cn_name AS relCnName
    private String relCnName;

    // 对应 SQL: RETURN end AS target
    // SDN 会自动把节点数据映射到这个 EntityNode 对象中
    private EntityNode target;
}
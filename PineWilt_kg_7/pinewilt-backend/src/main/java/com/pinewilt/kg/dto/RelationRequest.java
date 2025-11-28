package com.pinewilt.kg.dto;

import lombok.Data;

@Data
public class RelationRequest {
    private Long sourceId;      // 起点业务ID
    private Long targetId;      // 终点业务ID
    private String relType;     // 关系类型 (如 DAMAGES)
    private String cnName;      // 关系中文名 (如 危害)
}
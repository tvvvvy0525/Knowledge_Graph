package com.pinewilt.kg.dto;

import lombok.Data;

@Data
public class RelationTypeDto {
    private String relType;   // 关系类型 (英文)
    private String cnName;    // 中文名

    public RelationTypeDto(String relType, String cnName) {
        this.relType = relType;
        this.cnName = cnName;
    }
}
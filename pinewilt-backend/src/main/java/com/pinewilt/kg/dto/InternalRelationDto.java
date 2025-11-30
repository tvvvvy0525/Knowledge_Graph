package com.pinewilt.kg.dto;

import lombok.Data;

@Data
public class InternalRelationDto {
    private Long sourceId;
    private Long targetId;
    private String relType;
    private String relCnName;
}
package com.pinewilt.kg.dto;

import lombok.Data;

@Data
public class NodeRequest {
    private Long id;            // 业务ID (更新时必填)
    private String name;        // 英文名
    private String cnName;      // 中文名
    private String category;    // 类别
    private String description; // 描述
}
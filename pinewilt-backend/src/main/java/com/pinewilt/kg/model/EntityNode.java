package com.pinewilt.kg.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Node("Entity")
public class EntityNode {
    @Id
    @GeneratedValue
    private Long graphId;

    @Property("id")
    private Long id;

    @Property("name")
    private String name;

    // 将label_zh映射为label
    @Property("cn_name")
    private String cnName;

    // 将type映射为category
    @Property("category")
    private String category;

    @Property("description")
    private String description;

    // 添加toMap方法用于关系返回
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("graphId", graphId);
        map.put("name", name);
        map.put("cn_name", cnName);
        map.put("category", category);
        map.put("description", getDescription());
        return map;
    }

}

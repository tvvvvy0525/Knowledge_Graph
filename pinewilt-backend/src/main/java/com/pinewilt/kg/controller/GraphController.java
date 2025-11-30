package com.pinewilt.kg.controller;

import com.pinewilt.kg.dto.RelationTypeDto;
import com.pinewilt.kg.model.EntityNode;
import com.pinewilt.kg.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    // 1. GET /api/graph/init - 获取初始图谱数据
    @GetMapping("/init")
    public ResponseEntity<List<EntityNode>> getInitialGraph() {
        return ResponseEntity.ok(graphService.getInitialGraph());
    }

    // 2. GET /api/graph/neighbors/{id} - 获取节点邻居
    @GetMapping("/neighbors/{id}")
    public ResponseEntity<Map<String, Object>> getNeighbors(@PathVariable Long id) {
        return ResponseEntity.ok(graphService.getNeighbors(id));
    }

    // 3. GET /api/graph/search?q={kw} - 模糊搜索
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // Service 现在返回的是 Map { nodes: [], links: [] }
        return ResponseEntity.ok(graphService.search(q));
    }

    @GetMapping("/relation/types")
    public ResponseEntity<List<RelationTypeDto>> searchRelationTypes(@RequestParam String q) {
        return ResponseEntity.ok(graphService.searchRelationTypes(q));
    }
}
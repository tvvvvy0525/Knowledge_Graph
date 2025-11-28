package com.pinewilt.kg.controller;

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
    public ResponseEntity<List<EntityNode>> search(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(graphService.search(keyword));
    }
}
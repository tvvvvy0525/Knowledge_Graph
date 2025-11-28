package com.pinewilt.kg.controller;

import com.pinewilt.kg.dto.NodeRequest;
import com.pinewilt.kg.dto.RelationRequest;
import com.pinewilt.kg.model.EntityNode;
import com.pinewilt.kg.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manage")
@RequiredArgsConstructor
public class ManageController {

    private final GraphService graphService;

    // 4. POST /api/manage/node - 创建新节点
    @PostMapping("/node")
    public ResponseEntity<EntityNode> createNode(@RequestBody NodeRequest request) {
        return ResponseEntity.ok(graphService.createNode(request));
    }

    // 5. PUT /api/manage/node - 更新节点属性
    @PutMapping("/node")
    public ResponseEntity<EntityNode> updateNode(@RequestBody NodeRequest request) {
        return ResponseEntity.ok(graphService.updateNode(request));
    }

    // 6. DELETE /api/manage/node/{id} - 删除节点 (级联)
    @DeleteMapping("/node/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable Long id) {
        graphService.deleteNode(id);
        return ResponseEntity.ok().build();
    }

    // 7. POST /api/manage/relation - 创建关系
    @PostMapping("/relation")
    public ResponseEntity<String> createRelation(@RequestBody RelationRequest request) {
        graphService.createRelation(request);
        return ResponseEntity.ok("Relation created successfully");
    }
}
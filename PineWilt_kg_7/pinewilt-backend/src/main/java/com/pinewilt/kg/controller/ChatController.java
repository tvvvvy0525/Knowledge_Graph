package com.pinewilt.kg.controller;

import com.pinewilt.kg.service.PwdChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin // 允许前端跨域
public class ChatController {

    @Autowired
    private PwdChatService chatService;

    @GetMapping("/ask")
    public Map<String, String> ask(@RequestParam String question) {
        // 调用 Service 获取 AI 回答
        String answer = chatService.chat(question);
        return Map.of("answer", answer);
    }
}
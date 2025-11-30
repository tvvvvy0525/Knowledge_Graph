package com.pinewilt.kg.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PwdChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // Spring AI 自动注入 ChatClient.Builder 和 VectorStore
    public PwdChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public String chat(String userMessage) {
        // Step 1: 向量检索 (Vector Search)
        // 在向量库中查找最相似的 2 条记录
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.query(userMessage).withTopK(2)
        );

        // 提取文档内容
        String context = similarDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n---\n"));

        // Step 2: 构建提示词 (Prompt Engineering)
        // 如果没有检索到相关内容，context 可能是空的，AI 会依据自身知识回答
        String systemPrompt = """
                你是一个松材线虫病防治领域的资深专家。
                请基于以下【参考资料】回答用户的【问题】。
                如果参考资料中没有相关信息，请利用你的专业知识回答，并说明"知识库中未找到直接依据"。
                请保持回答简练、专业。
                """;

        String finalPrompt = String.format("""
                %s
                
                【参考资料】：
                %s
                
                【问题】：%s
                """, systemPrompt, context, userMessage);

        // Step 3: 调用 Ollama 大模型生成回答
        return chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();
    }
}
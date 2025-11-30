package com.pinewilt.kg.service;

import com.pinewilt.kg.repository.EntityNodeRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PwdChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Autowired
    private EntityNodeRepository entityNodeRepository;

    // Spring AI 自动注入 ChatClient.Builder 和 VectorStore
    public PwdChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public String chat(String userMessage) {
        if (userMessage.matches(".*(你好|你是谁|在吗|功能).*")) {
            return chatClient.prompt()
                    .user("用户在向你打招呼或询问身份。请用自然、专业的语气简单进行自我介绍，说明你是松材线虫病专家助手，能做诊断和防治咨询。不要废话。")
                    .call()
                    .content();
        }


        // Step 1: 向量检索 (Vector Search)
        // 在向量库中查找最相似的 2 条记录
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.query(userMessage).withTopK(2).withSimilarityThreshold(0.75)
        );

        // 提取文档内容
        String context = "";
        if (!similarDocs.isEmpty()) {
            context = similarDocs.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n---\n"));
        }

        List<String> graphRelations = entityNodeRepository.findGraphContext(userMessage);

        String graphContext = "";
        if (!graphRelations.isEmpty()) {
            // 将列表拼接成字符串，例如："松墨天牛 的 天敌 是 管氏肿腿蜂"
            graphContext = String.join("\n", graphRelations);
        }

        if (similarDocs.isEmpty() && graphContext.isEmpty()) {
            return chatClient.prompt()
                    .system("你是一个松材线虫病防治领域的资深专家。")
                    .user(String.format("""
                        用户问题：%s
                        
                        (注意：知识库中未找到相关资料，请仅凭你的通用专业知识回答，并礼貌地告知用户知识库中暂无直接依据)
                        """, userMessage))
                    .call()
                    .content();
        }

        // Step 2: 构建提示词 (Prompt Engineering)
        // 如果没有检索到相关内容，context 可能是空的，AI 会依据自身知识回答
        String systemText = """
                你是一个松材线虫病防治领域的资深专家助手。
                请综合利用以下【参考资料】回答用户问题。
                
                资料来源说明：
                1. 【法规与技术文档】：来自向量库，包含具体的规程、标准、描述。
                2. 【知识图谱关系】：来自图数据库，包含准确的实体关系（如天敌、媒介、寄主）。
                
                回答要求：
                - 优先使用图谱中的关系来回答实体关联类问题（如“A的天敌是谁”）。
                - 优先使用技术文档回答操作类问题（如“怎么取样”）。
                - 将两部分信息有机融合，回答要专业、流畅。
                """;

        String userText = String.format("""
                【用户问题】：%s
                
                ---
                【法规与技术文档 (Vector)】：
                %s
                
                ---
                【知识图谱关系 (Graph)】：
                %s
                ---
                """, userMessage,
                context.isEmpty() ? "无相关文档" : context,
                graphContext.isEmpty() ? "无相关图谱节点" : graphContext
        );

        // Step 3: 调用 Ollama 大模型生成回答
        return chatClient.prompt()
                .system(systemText)
                .user(userText)
                .call()
                .content();
    }
}
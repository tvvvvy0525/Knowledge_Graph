package com.pinewilt.kg.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class VectorStoreLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreLoader.class);

    private final VectorStore vectorStore;

    @Value("classpath:knowledge_data.json")
    private Resource jsonFile;

    public VectorStoreLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) {
        try {
            // 1. 读取 JSON
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> dataList = mapper.readValue(
                    jsonFile.getInputStream(),
                    new TypeReference<List<Map<String, String>>>() {}
            );

            // 2. 转换为 Document 对象
            List<Document> documents = new ArrayList<>();
            for (Map<String, String> item : dataList) {
                String question = item.get("instruction");
                String answer = item.get("output");

                // 将 "问题+答案" 拼在一起作为向量化的内容，提高检索匹配度
                String content = "问题：" + question + "\n答案：" + answer;

                // 将原始问题作为元数据存储，方便后续查看
                Document doc = new Document(content, Map.of("original_question", question));
                documents.add(doc);
            }

            // 3. 写入 Neo4j 向量索引
            // 注意：这里简单实现，每次启动都会添加。
            // 如果不想重复，可以在 Neo4j 里手动清空，或者在这里加个判断逻辑。
            vectorStore.add(documents);

            logger.info("✅ 成功加载 {} 条知识到向量数据库！", documents.size());

        } catch (Exception e) {
            logger.error("❌ 加载向量数据失败: {}", e.getMessage());
        }
    }
}
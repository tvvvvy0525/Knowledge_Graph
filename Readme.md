# 🌲 松材线虫病知识图谱智能系统 (Pine Wilt Disease Knowledge System)

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-green) ![Vue](https://img.shields.io/badge/Vue-3.3-42b883) ![Neo4j](https://img.shields.io/badge/Neo4j-5.15+-blue) ![Ollama](https://img.shields.io/badge/AI-Ollama-orange)

> 基于 **知识图谱 (Knowledge Graph)** 与 **大语言模型 (LLM)** 的领域专家系统。本项目旨在通过图谱可视化、智能诊断模型和 RAG（检索增强生成）技术，为松材线虫病的防治提供科学决策支持。

---

## ✨ 核心功能 (Features)

### 1. 🕸️ 知识图谱可视化与管理
-   **力导向图展示**：使用 ECharts 实现节点与关系的动态交互展示。
-   **CRUD 管理**：支持前端可视化的实体（病原、媒介、寄主等）与关系的增删改查。
-   **多跳查询**：支持查询实体的邻居节点及跨层级关系（如：寄主 -> 种类 -> 具体松树）。

### 2. 🤖 智能问答助手 (Hybrid RAG)
-   **双路召回机制**：结合 **向量检索 (Vector Search)** 处理非结构化文本（法规、标准）与 **图谱检索 (Graph Search)** 处理结构化关系。
-   **本地大模型集成**：通过 **Spring AI** 对接本地 **Ollama (Qwen 2.5)**，数据隐私安全，无 API 调用成本。
-   **闲聊与专业模式切换**：智能识别用户意图，闲聊时自然回复，专业问题时引用知识库。

### 3. 🩺 智能风险诊断 (Smart Diagnosis)
-   **定量评估模型**：内置基于媒介昆虫、寄主植物、环境气象（温度/降雨）等多因子的风险评估算法。
-   **向导式交互**：提供 Step-by-Step 的表单向导，自动计算风险指数 ($D_0$) 并给出红/黄/绿风险等级预警。
-   **防治建议生成**：根据风险等级自动匹配并展示相应的防治策略（如疫区封锁、生物防治等）。

---

## 🛠️ 技术栈 (Tech Stack)

| 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **后端** | Java 21, Spring Boot 3.2.4 | 核心业务逻辑 |
| **AI 框架** | **Spring AI** | 统一接口对接 Ollama 与 Vector Store |
| **数据库** | **Neo4j (v5.15+)** | 图数据库 + 向量数据库 (Vector Index) |
| **大模型** | **Ollama (Qwen 2.5-7B)** | 本地运行的推理模型 |
| **前端** | Vue 3, Vite, Pinia | 响应式单页应用 |
| **UI/可视化** | Element Plus, ECharts | 组件库与图表渲染 |

---

## 🚀 环境准备 (Prerequisites)

在运行项目前，请确保你的本地环境已安装以下软件：

1.  **Java Development Kit (JDK)**: 版本 21+
2.  **Node.js**: 版本 18+ (推荐使用 LTS)
3.  **Neo4j Database**: 版本 **5.15 或更高** (必须支持 Vector Index)
    *   *注意：旧版本 Neo4j 不支持向量索引，会导致后端启动失败。*
4.  **Ollama**: 用于运行本地大模型。
    *   下载地址: [ollama.com](https://ollama.com)

---

## 📦 快速开始 (Installation)

### 第一步：启动 AI 模型 (Ollama)

在终端执行以下命令，拉取并运行通义千问模型：

```bash
# 拉取模型 (约 4GB 显存需求)
ollama pull qwen2.5:7b

# 启动模型服务 (默认端口 11434)
ollama run qwen2.5:7b
```

### 第二步：配置并启动后端

1.  进入后端目录 `pinewilt-backend`。
2.  修改 `src/main/resources/application.yml`：
    ```yaml
    spring:
      neo4j:
        uri: bolt://localhost:7687
        authentication:
          username: neo4j
          password: [你的密码] # <--- 修改这里
    ```
3.  **数据初始化**：
    *   项目启动时，`VectorStoreLoader` 会自动读取 `src/main/resources/knowledge_data.json` 并将向量数据导入 Neo4j。
    *   *提示：若需清空向量库，Loader 中已包含自动清理逻辑。*
4.  启动 Spring Boot 应用。

### 第三步：启动前端

1.  进入前端目录 `pine-wilt-sys`。
2.  安装依赖并启动：
    ```bash
    npm install
    npm run dev
    ```
3.  访问浏览器：`http://localhost:5173`

---

## 📂 项目结构 (Project Structure)

```text
├── pinewilt-backend (后端)
│   ├── src/main/java/com/pinewilt/kg
│   │   ├── config/          # VectorStoreLoader (向量加载器)
│   │   ├── controller/      # GraphController, ChatController (API 接口)
│   │   ├── model/           # EntityNode (图节点定义)
│   │   ├── repository/      # Neo4j Repositories (含自定义 Cypher 查询)
│   │   └── service/         # PwdChatService (RAG 核心逻辑), DiagnosisService
│   └── src/main/resources
│       ├── application.yml  # 配置文件
│       └── knowledge_data.json # 本地知识库源文件
│
└── pine-wilt-sys (前端)
    ├── src
    │   ├── api/             # Axios 请求封装
    │   ├── components/
    │   │   ├── AIChatBox.vue    # AI 悬浮聊天窗
    │   │   ├── GraphChart.vue   # ECharts 图谱组件
    │   │   └── DiagnosisWizard.vue # 智能诊断向导
    │   ├── stores/          # Pinia 状态管理
    │   └── views/           # HomeView.vue
    └── package.json
```

---

## 🧠 Graph RAG 原理说明

本项目采用了 **混合检索 (Hybrid Retrieval)** 策略来增强 AI 的回答准确性：

1.  **用户提问**：例如 "松墨天牛的天敌是谁？"
2.  **路经 A (Vector Search)**：将问题转化为向量，在 Neo4j 向量索引中检索相似的 JSON 文本块（法规、描述）。
3.  **路经 B (Graph Search)**：通过 Cypher 查询在图谱中检索实体及其邻居关系（如 `松墨天牛 -[天敌]-> 管氏肿腿蜂`）。
4.  **上下文融合**：将 A 和 B 的结果拼接，作为 Context 输入给 Ollama。
5.  **LLM 生成**：大模型基于 Context 生成最终回答，并标注信息来源。


---

## 📝 License

[MIT](https://opensource.org/licenses/MIT)

---

Copyright © 2024 Pine Wilt Disease Knowledge Engineering Group.
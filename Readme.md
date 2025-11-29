# 松材线虫病知识图谱系统 (Pine Wilt Disease Knowledge System)

> 知识工程结课作业 | 知识图谱可视化与智能诊断系统

## 📖 项目简介

本项目构建了一个垂直领域的知识系统，深度整合“松材线虫病”的病理机制、媒介昆虫、宿主植物及环境影响因素。系统实现了**前后端分离**架构，前端基于 **Vue 3 + ECharts** 实现力导向图的动态交互，后端基于 **Spring Boot + Neo4j** 提供图数据的存储与复杂查询服务。

**当前状态 (2025.11)**：
- [x] **全栈联调**：前端与 Spring Boot 后端完全打通
- [x] **图谱交互**：支持点击节点动态展开邻居、缩放平移
- [x] **智能搜索**：支持中英文模糊搜索，直接展示关联子图
- [x] **数据管理**：支持实体的增删改、关系的动态创建
- [x] **动态分类**：支持新建实体时自定义分类，图例自动适配

---

## 🛠 技术栈

### 前端 (Frontend)
- **核心框架**: Vue 3 (Composition API) + Vite
- **可视化**: Apache ECharts 5.x
- **UI 组件**: Element Plus (用于弹窗、表单、侧边栏)
- **状态管理**: Pinia (处理图谱数据的合并、去重、状态同步)
- **网络请求**: Axios (拦截器封装)

### 后端 (Backend)
- **核心框架**: Spring Boot 3.x
- **图数据库**: Neo4j Community Edition 5.x
- **ORM 框架**: Spring Data Neo4j (SDN)
- **数据交互**: RESTful API + DTO Projection

---

## ✨ 核心功能

1.  **初始化与探索**：
    - 系统加载时自动以“松材线虫病”为核心展开一级邻居。
    - 点击任意节点，动态加载该节点的邻居节点并建立连线（防止数据爆炸）。
2.  **子图搜索**：
    - 输入关键词（如“松材线虫”），不仅返回节点，还返回节点之间的关系，形成有意义的子图。
3.  **可视化管理 (CRUD)**：
    - **新建实体**：支持自定义 ID 生成策略（GraphID + 1），支持自定义分类（Allow-Create）。
    - **新建关系**：支持源/目标节点的远程搜索，支持关系类型的自动联想与中文名填充。
    - **编辑/删除**：右侧面板支持属性的实时编辑和节点的级联删除。

---

## 🚀 快速开始

### 1. 环境准备
- **JDK**: 17+
- **Node.js**: 16+
- **Neo4j**: 启动本地服务 (默认端口 7687)

### 2. 后端启动 (Backend)
1. 修改 `src/main/resources/application.properties` 配置 Neo4j 账号密码。
2. 运行 `PinewiltKgApplication` 主类。
3. 服务将运行在 `http://localhost:8080`。

### 3. 前端启动 (Frontend)
```bash
# 进入前端目录
cd pinewilt-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```
访问 `http://localhost:3000` (Vite 配置了 Proxy 代理 `/api` 到 8080)。

---

## 📥 数据库初始化 (Data Import)

本项目使用 Cypher 脚本从 CSV 文件导入初始数据。

**前置要求**：
1. 请确保 Neo4j 数据库已安装 **APOC 插件** (用于动态创建关系)。
2. 将 `nodes.csv` 和 `relationship.csv` 文件放入 Neo4j 安装目录的 `import` 文件夹中。

### 1. 导入节点 (Nodes)
执行以下 Cypher 语句导入实体节点。注意：我们将 CSV 中的 `cn_label` 列映射为了数据库中的 `cn_name` 属性。

```cypher
// 读取 CSV 文件
LOAD CSV WITH HEADERS FROM 'file:///nodes.csv' AS row

// 创建节点
// 统一添加基础标签 :Entity
CREATE (n:Entity {
    id: toInteger(row.id),           // 转换 ID 为整数，优化查询性能
    name: row.name,                  // 英文名
    cn_name: row.cn_label,           // 中文名 (对应 CSV 表头 cn_label)
    category: row.category,          // 类别
    description: row.description     // 描述
});
```

### 2. 导入关系 (Relationships)
执行以下 Cypher 语句建立节点间的关联。此步骤使用了 `apoc.create.relationship` 以支持从 CSV 动态读取关系类型（如 "DAMAGES", "INFECTS" 等）。

```cypher
LOAD CSV WITH HEADERS FROM 'file:///relationship.csv' AS row

// 匹配源节点和目标节点
MATCH (source:Entity {id: toInteger(row.source_id)})
MATCH (target:Entity {id: toInteger(row.target_id)})

// 动态创建关系
// 参数: 起点, 关系类型(来自CSV), 属性(中文名), 终点
CALL apoc.create.relationship(source, row.rel_type, {cn_name: row.cn_name}, target) YIELD rel

RETURN count(rel);
```

### 3. 创建索引 (推荐)
为了加快 `id` 的查找速度（解决点击节点 404/500 问题），建议为业务 ID 创建索引：

```cypher
CREATE INDEX FOR (n:Entity) ON (n.id);
```

## 📂 项目结构

### 前端 (`pine-wilt-sys`)

```text
pine-wilt-sys/
└── src/
    ├── api/
    │   └── graph.js          # 统一封装 RESTful API 请求
    ├── components/
    │   ├── GraphChart.vue    # ECharts 核心可视化组件
    │   ├── InfoPanel.vue     # 右侧详情/编辑面板
    │   ├── NodeCreateDialog.vue # 新建节点弹窗
    │   ├── LinkCreateDialog.vue # 新建关系弹窗
    │   └── SearchBar.vue     # 顶部搜索栏
    ├── stores/
    │   └── graphStore.js     # Pinia 状态仓库 (核心业务逻辑)
    ├── utils/
    │   └── request.js        # Axios 实例与拦截器
    └── views/
        └── HomeView.vue      # 页面主布局
```

### 后端 (`pinewilt-backend`)

```text
pinewilt-backend/
└── src/
    └── main/
        ├── java/
        │   └── com/pinewilt/kg/
        │       ├── controller/ # 控制层 (REST API 入口)
        │       │   ├── GraphController.java  # 数据查询接口
        │       │   └── ManageController.java # 数据管理接口
        │       ├── dto/        # 数据传输对象 (DTO)
        │       ├── model/      # Neo4j 节点实体模型
        │       │   └── EntityNode.java
        │       ├── repository/ # Spring Data Neo4j Repository
        │       │   └── EntityNodeRepository.java
        │       ├── service/    # 业务逻辑层
        │       │   └── GraphService.java
        │       └── PinewiltKgApplication.java # Spring Boot 启动类
        └── resources/
            └── application.yml # 配置文件 (数据库连接、端口等)
```

---

## 🔌 接口规范 (API Reference)

后端提供标准的 RESTful 接口，所有接口前缀为 `/api`。

### 1. 读接口 (Read)

| 方法 | URL | 描述 | 返回结构 |
| :--- | :--- | :--- | :--- |
| `GET` | `/graph/neighbors/{id}` | 获取节点详情及邻居 (展开用) | `{ node: {}, neighbors: { "TYPE": [...] } }` |
| `GET` | `/graph/search?q={kw}` | 模糊搜索 (返回子图) | `{ nodes: [], links: [] }` |
| `GET` | `/graph/relation/types` | 搜索已有的关系类型 | `[ { relType: "...", cnName: "..." } ]` |

### 2. 写接口 (Write)

| 方法 | URL | 描述 | 参数示例 |
| :--- | :--- | :--- | :--- |
| `POST` | `/manage/node` | 创建新节点 | `{ name, cnName, category, description }` |
| `PUT` | `/manage/node` | 更新节点属性 | `{ id, name, ... }` |
| `DELETE` | `/manage/node/{id}` | 级联删除节点 | - |
| `POST` | `/manage/relation` | 创建新关系 | `{ sourceId, targetId, relType, cnName }` |

---

## 🗓 开发进度 (Roadmap)

### Phase 1: 基础构建 (已完成 ✅)
- [x] ECharts 可视化集成
- [x] Spring Boot + Neo4j 环境搭建
- [x] 基础的点边展示与交互

### Phase 2: 数据交互与管理 (已完成 ✅)
- [x] **动态展开**：修复了 ID 类型不匹配导致的 404/500 问题。
- [x] **搜索增强**：实现了“搜节点带连线”的子图查询。
- [x] **完整 CRUD**：实现了前端弹窗与后端事务管理的对接。
- [x] **关系管理**：实现了关系类型的远程搜索与自动补全。

### Phase 3: 智能化 (待开发 📅)
- [ ] 知识推理：基于规则的潜在关系预测。
- [ ] LLM 集成：接入大模型实现自然语言问答 (Text-to-Cypher)。


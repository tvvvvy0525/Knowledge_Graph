# 松材线虫病知识图谱系统 (Pine Wilt Disease Knowledge System)

> 知识工程结课作业 | 知识图谱可视化与智能诊断系统

## 📖 项目简介

本项目旨在构建一个垂直领域的知识系统，整合“松材线虫病”的病理、媒介昆虫、宿主植物及环境影响因素。系统前端基于 **Vue 3 + ECharts** 实现知识图谱的力导向可视化，支持动态交互；后端（开发中）计划采用 **Spring Boot + Neo4j**。

**当前状态**：
- [x] 前端可视化框架已搭建
- [x] 核心交互（展开/收起/详情）已完成
- [x] 纯前端 Mock 数据模式（可独立演示）
- [ ] 后端接口对接中

---

## 🛠 技术栈

### 前端 (Current)
- **核心框架**: Vue 3 (Composition API) + Vite
- **可视化**: Apache ECharts 5.x
- **UI 组件**: Element Plus
- **状态管理**: Pinia
- **网络请求**: Axios

### 后端 (Planned)
- **框架**: Spring Boot 3.x
- **数据库**: Neo4j Community
- **AI/LLM**: LangChain4j (用于智能问答)

---

## 🚀 快速开始 (Frontend)

本项目目前内置了 **Mock 模式**，无需后端即可启动并体验完整交互。

### 1. 安装依赖
```bash
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```
访问控制台输出的地址（通常是 `http://localhost:5173`）。

### 3. 项目结构说明
```text
src/
├── api/
│   └── graph.js          # 接口层 (当前包含 Mock 数据，对接后端时需修改)
├── components/
│   └── GraphChart.vue    # ECharts 图谱核心组件
├── stores/
│   └── graphStore.js     # Pinia 状态管理 (处理图谱数据的合并、展开、收起逻辑)
├── views/
│   └── HomeView.vue      # 主页面 (布局容器)
└── utils/
    └── request.js        # Axios 封装
```

---

## 🤝 后端接口开发指南 (For Collaborators)

前端 `src/api/graph.js` 目前使用模拟数据。后端开发人员需按照以下数据结构提供 RESTful API。

### 1. 基础数据结构约定
**节点 (Node)**
```json
{
  "id": "String (UUID)",
  "name": "String (显示名称)",
  "category": "String (分类: 病害/昆虫/植物/环境)",
  "symbolSize": "Number (可选, 推荐 30-50)",
  "description": "String (详情描述)"
}
```

**关系 (Link)**
```json
{
  "source": "String (Node ID)",
  "target": "String (Node ID)",
  "name": "String (关系名称)"
}
```

### 2. 必需接口清单

#### A. 获取初始图谱
- **URL**: `GET /api/graph/init`
- **功能**: 返回中心节点（如“松材线虫病”）及其直接关联的子图。
- **Response**:
```json
{
  "code": 200,
  "data": {
    "nodes": [ ... ],
    "links": [ ... ]
  }
}
```

#### B. 获取邻居节点 (展开功能)
- **URL**: `GET /api/graph/neighbors/{nodeId}`
- **功能**: 根据节点ID，返回该节点的所有邻居及连线。前端会自动去重合并。
- **Response**: 同上。

---

## 🗓 开发计划 (Roadmap)

### Phase 1: 基础可视化 (已完成 ✅)
- 项目初始化与 ECharts 集成
- Mock 数据驱动的点击展开/收起
- 侧边栏详情展示

### Phase 2: 数据管理 CRUD (进行中 🚧)
- [ ] 实体增删改查 API 对接
- [ ] 侧边栏编辑模式开发
- [ ] 新建关联关系交互

### Phase 3: 搜索与诊断 (待定 📅)
- [ ] 全文检索与图谱定位
- [ ] 基于规则的专家诊断系统
- [ ] LLM 智能问答集成

---

## 🔧 常见问题

**Q: 如何切换到真实后端？**
A: 
1. 确保后端服务运行在 `localhost:8080`。
2. 打开 `vite.config.js`，确认 `proxy` 配置已开启。
3. 修改 `src/api/graph.js`，注释掉 Mock 代码，启用真实的 `request.get(...)` 调用。

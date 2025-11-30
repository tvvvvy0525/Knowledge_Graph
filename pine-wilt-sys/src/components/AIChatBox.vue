<template>
    <div class="ai-chat-container">
      <!-- 1. 悬浮按钮 (当窗口关闭时显示) -->
      <transition name="el-zoom-in-center">
        <div v-show="!isVisible" class="float-btn" @click="openChat">
          <el-icon size="24" color="#fff"><ChatDotRound /></el-icon>
          <span class="btn-text">AI 助手</span>
        </div>
      </transition>
  
      <!-- 2. 聊天窗口 (当窗口打开时显示) -->
      <transition name="el-zoom-in-bottom">
        <el-card v-show="isVisible" class="chat-window" :body-style="{ padding: '0' }">
          
          <!-- 头部 -->
          <template #header>
            <div class="chat-header">
              <div class="header-left">
                <el-avatar :size="30" :icon="Service" class="ai-avatar" />
                <span class="title">松材线虫病专家 AI</span>
              </div>
              <el-button link @click="closeChat">
                <el-icon size="20"><Close /></el-icon>
              </el-button>
            </div>
          </template>
  
          <!-- 消息内容区域 -->
          <div class="chat-body" ref="chatBodyRef">
            <!-- 欢迎语 -->
            <div class="message ai">
              <el-avatar :size="30" :icon="Service" class="msg-avatar" />
              <div class="bubble">
                你好！我是松材线虫病防治领域的智能助手。您可以问我关于<b>取样标准、感病特征、防治药剂</b>等问题。
              </div>
            </div>
  
            <!-- 循环渲染消息 -->
            <div v-for="(msg, index) in messageList" :key="index" :class="['message', msg.role]">
              <!-- AI 头像 -->
              <el-avatar v-if="msg.role === 'ai'" :size="30" :icon="Service" class="msg-avatar" />
              
              <!-- 消息气泡 -->
              <div class="bubble">
                {{ msg.content }}
              </div>
  
              <!-- 用户头像 -->
              <el-avatar v-if="msg.role === 'user'" :size="30" :icon="UserFilled" class="msg-avatar" />
            </div>
  
            <!-- 加载动画 -->
            <div v-if="loading" class="message ai">
              <el-avatar :size="30" :icon="Service" class="msg-avatar" />
              <div class="bubble loading-bubble">
                <span class="dot">.</span><span class="dot">.</span><span class="dot">.</span>
              </div>
            </div>
          </div>
  
          <!-- 底部输入框 -->
          <div class="chat-footer">
            <el-input
              v-model="inputText"
              placeholder="请输入您的问题..."
              @keyup.enter="handleSend"
              :disabled="loading"
            >
              <template #append>
                <el-button @click="handleSend" :loading="loading" :icon="Position" />
              </template>
            </el-input>
          </div>
  
        </el-card>
      </transition>
    </div>
  </template>
  
  <script setup>
  import { ref, nextTick } from 'vue'
  import { askAI } from '@/api/chat'
  // 引入图标
  import { ChatDotRound, Close, Service, UserFilled, Position } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
  
  // --- 状态定义 ---
  const isVisible = ref(false) // 窗口是否可见
  const inputText = ref('')    // 输入框内容
  const loading = ref(false)   // AI是否正在思考
  const chatBodyRef = ref(null)// 滚动区域引用
  
  // 消息列表
  const messageList = ref([
    // 示例数据结构： { role: 'user' | 'ai', content: 'xxx' }
  ])
  
  // --- 方法 ---
  
  // 打开窗口
  const openChat = () => {
    isVisible.value = true
    scrollToBottom()
  }
  
  // 关闭窗口
  const closeChat = () => {
    isVisible.value = false
  }
  
  // 发送消息
  const handleSend = async () => {
    const content = inputText.value.trim()
    if (!content) return
  
    // 1. 添加用户消息
    messageList.value.push({ role: 'user', content: content })
    inputText.value = '' // 清空输入框
    loading.value = true
    scrollToBottom()
  
    try {
      // 2. 调用后端 API
      const res = await askAI(content)
      
      // 3. 添加 AI 回复 (假设后端返回结构是 { answer: "xxx" })
      if (res && res.answer) {
        messageList.value.push({ role: 'ai', content: res.answer })
      } else {
        messageList.value.push({ role: 'ai', content: '抱歉，我没有获取到有效的回答。' })
      }
    } catch (error) {
      console.error(error)
      messageList.value.push({ role: 'ai', content: '网络连接异常，请检查后端服务是否启动。' })
    } finally {
      loading.value = false
      scrollToBottom()
    }
  }
  
  // 滚动到底部
  const scrollToBottom = () => {
    nextTick(() => {
      if (chatBodyRef.value) {
        chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
      }
    })
  }
  </script>
  
  <style scoped>
  /* 容器定位：固定在右下角 */
  .ai-chat-container {
    position: fixed;
    bottom: 30px;
    right: 30px;
    z-index: 2000; /* 保证在最上层 */
  }
  
  /* 悬浮按钮样式 */
  .float-btn {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, #409EFF, #337ecc);
    border-radius: 50%;
    box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: transform 0.3s;
  }
  .float-btn:hover {
    transform: scale(1.1);
  }
  .btn-text {
    font-size: 10px;
    color: #fff;
    margin-top: 2px;
  }
  
  /* 聊天窗口样式 */
  .chat-window {
    width: 380px;
    height: 550px;
    display: flex;
    flex-direction: column;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }
  
  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #f5f7fa;
    padding: 10px 15px;
    border-bottom: 1px solid #e4e7ed;
  }
  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .title {
    font-weight: bold;
    color: #303133;
    font-size: 15px;
  }
  .ai-avatar {
    background-color: #409EFF;
  }
  
  /* 消息主体区域 */
  .chat-body {
    height: 430px; /* 固定高度 */
    overflow-y: auto;
    padding: 15px;
    background-color: #fff;
    display: flex;
    flex-direction: column;
    gap: 15px;
  }
  
  /* 消息行 */
  .message {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    max-width: 100%;
  }
  .message.user {
    flex-direction: row-reverse; /* 用户消息靠右 */
  }
  .message.ai {
    flex-direction: row; /* AI 消息靠左 */
  }
  
  /* 气泡样式 */
  .bubble {
    padding: 10px 14px;
    border-radius: 8px;
    font-size: 14px;
    line-height: 1.5;
    max-width: 75%;
    word-break: break-all;
    white-space: pre-wrap; /* 保留换行符 */
  }
  
  /* 颜色区分 */
  .message.ai .bubble {
    background-color: #f4f4f5;
    color: #303133;
    border-top-left-radius: 0;
  }
  .message.user .bubble {
    background-color: #ecf5ff; /* 浅蓝 */
    color: #409EFF;
    border: 1px solid #d9ecff;
    border-top-right-radius: 0;
  }
  
  .msg-avatar {
    flex-shrink: 0;
  }
  
  /* 底部输入区 */
  .chat-footer {
    padding: 10px 15px;
    border-top: 1px solid #e4e7ed;
    background-color: #fff;
  }
  
  /* 加载动画的小点 */
  .loading-bubble {
    display: flex;
    gap: 3px;
  }
  .dot {
    animation: jump 1.5s infinite;
  }
  .dot:nth-child(2) { animation-delay: 0.2s; }
  .dot:nth-child(3) { animation-delay: 0.4s; }
  
  @keyframes jump {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-3px); }
  }
  </style>
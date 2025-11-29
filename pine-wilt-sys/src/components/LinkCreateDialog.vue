<template>
    <el-dialog v-model="visible" title="新建关联关系" width="500px">
      <el-form :model="form" label-width="100px">
        
        <!-- 源节点搜索 -->
        <el-form-item label="源节点">
          <el-select
            v-model="form.sourceId"
            filterable
            remote
            placeholder="搜索源节点名称"
            :remote-method="searchSource"
            :loading="loadingSource"
            style="width: 100%"
          >
            <el-option
              v-for="item in sourceOptions"
              :key="item.id"
              :label="item.cnName || item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
  
        <!-- 目标节点搜索 -->
        <el-form-item label="目标节点">
          <el-select
            v-model="form.targetId"
            filterable
            remote
            placeholder="搜索目标节点名称"
            :remote-method="searchTarget"
            :loading="loadingTarget"
            style="width: 100%"
          >
            <el-option
              v-for="item in targetOptions"
              :key="item.id"
              :label="item.cnName || item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
  
        <el-form-item label="关系类型">
          <!-- 为了简化，这里可以是下拉选，也可以是手输 -->
          <el-select v-model="form.relType" allow-create filterable placeholder="选择或输入英文类型 (如 DAMAGES)">
            <el-option label="DAMAGES" value="DAMAGES" />
            <el-option label="CAUSED_BY" value="CAUSED_BY" />
            <el-option label="TRANSMITS" value="TRANSMITS" />
            <el-option label="HAS_SYMPTOM" value="HAS_SYMPTOM" />
          </el-select>
        </el-form-item>
  
        <el-form-item label="关系中文名">
          <el-input v-model="form.cnName" placeholder="例如: 危害" />
        </el-form-item>
  
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">创建连接</el-button>
      </template>
    </el-dialog>
  </template>
  
  <script setup>
  import { ref, reactive } from 'vue'
  import { useGraphStore } from '@/stores/graphStore'
  import { searchNodes } from '@/api/graph' // 直接调用API做下拉框搜索
  
  const visible = ref(false)
  const submitting = ref(false)
  const store = useGraphStore()
  
  // 下拉框搜索状态
  const loadingSource = ref(false)
  const loadingTarget = ref(false)
  const sourceOptions = ref([])
  const targetOptions = ref([])
  
  const form = reactive({
    sourceId: null,
    targetId: null,
    relType: '',
    cnName: ''
  })
  
  const open = () => {
    form.sourceId = null
    form.targetId = null
    form.relType = ''
    form.cnName = ''
    sourceOptions.value = []
    targetOptions.value = []
    visible.value = true
  }
  
  // 远程搜索节点逻辑
  const handleRemoteSearch = async (query, isSource) => {
    if (!query) return
    if (isSource) loadingSource.value = true
    else loadingTarget.value = true
    
    try {
      const res = await searchNodes(query)
      if (isSource) sourceOptions.value = res
      else targetOptions.value = res
    } finally {
      if (isSource) loadingSource.value = false
      else loadingTarget.value = false
    }
  }
  
  const searchSource = (q) => handleRemoteSearch(q, true)
  const searchTarget = (q) => handleRemoteSearch(q, false)
  
  const handleSubmit = async () => {
    if (!form.sourceId || !form.targetId || !form.relType) return
    submitting.value = true
    try {
      await store.addLink({ ...form })
      visible.value = false
    } finally {
      submitting.value = false
    }
  }
  
  defineExpose({ open })
  </script>
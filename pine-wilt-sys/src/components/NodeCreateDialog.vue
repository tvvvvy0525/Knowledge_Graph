<template>
    <el-dialog v-model="visible" title="新建实体节点" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="英文名称">
          <el-input v-model="form.name" placeholder="例如: Pine Wilt Disease" />
        </el-form-item>
        <el-form-item label="中文名称">
          <el-input v-model="form.cnName" placeholder="例如: 松材线虫病" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.category" placeholder="请选择类型" style="width: 100%">
            <el-option label="病害 (Disease)" value="Disease" />
            <el-option label="昆虫 (Insect)" value="Insect" />
            <el-option label="植物 (Plant)" value="Plant" />
            <el-option label="环境 (Environment)" value="Environment" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
  </template>
  
  <script setup>
  import { ref, reactive } from 'vue'
  import { useGraphStore } from '@/stores/graphStore'
  
  const visible = ref(false)
  const loading = ref(false)
  const store = useGraphStore()
  
  const form = reactive({
    name: '',
    cnName: '',
    category: '',
    description: ''
  })
  
  const open = () => {
    // 重置表单
    form.name = ''
    form.cnName = ''
    form.category = ''
    form.description = ''
    visible.value = true
  }
  
  const handleSubmit = async () => {
    loading.value = true
    try {
      await store.addNode({ ...form })
      visible.value = false
    } finally {
      loading.value = false
    }
  }
  
  // 暴露 open 方法给父组件调用
  defineExpose({ open })
  </script>
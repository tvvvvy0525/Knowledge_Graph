<template>
  <el-dialog v-model="visible" title="新建实体节点" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="英文名称">
        <el-input v-model="form.name" placeholder="例如: Bursaphelenchus Xylophilus" />
      </el-form-item>

      <el-form-item label="中文名称">
        <el-input v-model="form.cnName" placeholder="例如: 松材线虫" />
      </el-form-item>

      <!-- 核心修改：支持搜索和自定义创建 -->
      <el-form-item label="类型">
        <el-select
            v-model="form.category"
            placeholder="请选择或输入新类型"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
            clearable
        >
          <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="描述">
        <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入节点描述信息"
        />
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

// 预置的建议列表（不是强制限制，用户可以输入列表之外的内容）
// 这样既保证了规范性，又给了灵活性
const categoryOptions = [
  { value: 'Pathogen & Disease', label: '病原与病害 (Pathogen & Disease)' },
  { value: 'Biocontrol', label: '生物防治 (Biocontrol)' },
  { value: 'Vector Insect', label: '媒介昆虫 (Vector Insect)' },
  { value: 'Host Plant', label: '寄主植物 (Host Plant)' },
  { value: 'Symptom', label: '症状表现 (Symptom)' },
  { value: 'Disease Feature', label: '病害特征 (Disease Feature)' },
  { value: 'Risk Assessment', label: '风险评估 (Risk Assessment)' },
  { value: 'Environment & Risk Factor', label: '环境与风险因子 (Environment & Risk Factor)' },
  { value: 'Assessment Result', label: '评估结果 (Assessment Result)' },
  { value: 'Prevention Method', label: '防治措施 (Prevention Method)' }
]

const open = () => {
  // 重置表单
  form.name = ''
  form.cnName = ''
  form.category = ''
  form.description = ''
  visible.value = true
}

const handleSubmit = async () => {
  // 简单的必填校验
  if (!form.name && !form.cnName) {
    // 实际项目中建议引入 ElementPlus 的 Message 提示
    alert('请至少输入名称')
    return
  }

  loading.value = true
  try {
    await store.addNode({ ...form })
    visible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 暴露 open 方法给父组件
defineExpose({ open })
</script>
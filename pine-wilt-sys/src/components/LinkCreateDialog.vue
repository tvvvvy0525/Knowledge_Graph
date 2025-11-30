<template>
  <el-dialog v-model="visible" title="新建关联关系" width="500px">
    <el-form :model="form" label-width="100px">

      <!-- 源节点搜索 (保持不变) -->
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

      <!-- 目标节点搜索 (保持不变) -->
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

      <!-- 【核心修改】关系类型：支持远程搜索 + 手动创建 -->
      <el-form-item label="关系类型">
        <el-select
            v-model="form.relType"
            allow-create
            filterable
            remote
            default-first-option
            placeholder="输入类型搜索，不存在可回车创建"
            :remote-method="handleRelTypeSearch"
            :loading="loadingRel"
            @change="handleRelTypeChange"
            style="width: 100%"
        >
          <el-option
              v-for="item in relOptions"
              :key="item.relType"
              :label="item.relType"
              :value="item.relType"
          >
            <!-- 自定义下拉选项模板，显示中文名辅助 -->
            <span style="float: left">{{ item.relType }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              {{ item.cnName }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="关系中文名">
        <el-input
            v-model="form.cnName"
            placeholder="例如: 危害"
        />
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
import { searchNodes, searchRelationTypes } from '@/api/graph' // 引入新API

const visible = ref(false)
const submitting = ref(false)
const store = useGraphStore()

// 节点搜索状态
const loadingSource = ref(false)
const loadingTarget = ref(false)
const sourceOptions = ref([])
const targetOptions = ref([])

// 关系类型搜索状态
const loadingRel = ref(false)
const relOptions = ref([])

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
  relOptions.value = []
  visible.value = true
}

// === 节点搜索逻辑 (适配 Map 结构) ===
const handleRemoteSearch = async (query, isSource) => {
  if (!query) return
  if (isSource) loadingSource.value = true
  else loadingTarget.value = true

  try {
    const res = await searchNodes(query)
    // 兼容后端返回 {nodes: [], links: []}
    const nodeList = res.nodes || []
    if (isSource) sourceOptions.value = nodeList
    else targetOptions.value = nodeList
  } finally {
    if (isSource) loadingSource.value = false
    else loadingTarget.value = false
  }
}
const searchSource = (q) => handleRemoteSearch(q, true)
const searchTarget = (q) => handleRemoteSearch(q, false)

// === 【核心修改】关系类型搜索逻辑 ===
const handleRelTypeSearch = async (query) => {
  if (!query) return
  loadingRel.value = true
  try {
    // 调用后端查已有关系
    const res = await searchRelationTypes(query)
    relOptions.value = res // res 是 [{relType: 'DAMAGES', cnName: '危害'}, ...]
  } catch (e) {
    console.error(e)
  } finally {
    loadingRel.value = false
  }
}

// 当用户选中下拉框某一项时触发
const handleRelTypeChange = (val) => {
  // val 是选中的 relType (因为 el-option 的 value 绑定的是 item.relType)

  // 在 options 里找对应的对象
  const selectedItem = relOptions.value.find(item => item.relType === val)

  if (selectedItem && selectedItem.cnName) {
    // 如果找到了，并且有中文名，自动填入
    form.cnName = selectedItem.cnName
  }
  // 如果没找到（说明是用户手输的新类型），就不自动填，保持原样或让用户手输
}

const handleSubmit = async () => {
  if (!form.sourceId || !form.targetId || !form.relType) {
    // 可以加个提示
    return
  }
  submitting.value = true
  try {
    await store.addLink({ ...form })
    visible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
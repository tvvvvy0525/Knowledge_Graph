<template>
  <div class="info-panel-container">
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>节点详情</span>
          <div v-if="currentNode">
            <!-- 切换编辑模式按钮 -->
            <el-button
              v-if="!isEditing"
              type="primary"
              link
              @click="isEditing = true"
            >
              编辑
            </el-button>
            <el-button v-else type="success" link @click="handleSave">
              保存
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="currentNode" class="panel-content">
        <!-- === 浏览模式 === -->
        <el-descriptions v-if="!isEditing" :column="1" border size="small">
          <el-descriptions-item label="中文名称">
            <strong>{{ currentNode.cnName }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="英文名称" v-if="currentNode.cn_name">
            {{ currentNode.name }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag size="small">{{ currentNode.category }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述">
            {{ currentNode.description || "暂无描述" }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- === 编辑模式 === -->
        <el-form v-else :model="editForm" label-position="top" size="small">
          <el-form-item label="英文名称 (Name)">
            <el-input v-model="editForm.name" />
          </el-form-item>
          <el-form-item label="中文名称 (CN Name)">
            <el-input v-model="editForm.cnName" />
          </el-form-item>
          <el-form-item label="类型 (Category)">
            <el-select
              v-model="editForm.category"
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
              v-model="editForm.description"
              type="textarea"
              :rows="4"
            />
          </el-form-item>
          <el-button @click="isEditing = false" size="small">取消</el-button>
        </el-form>
      </div>

      <div v-else class="empty-container">
        <el-empty description="点击节点查看详情" :image-size="60"></el-empty>
      </div>

      <!-- 底部删除按钮区 -->
      <div v-if="currentNode && !isEditing" class="panel-footer">
        <el-popconfirm
          title="确定要删除该节点及其关联关系吗?"
          @confirm="handleDelete"
        >
          <template #reference>
            <el-button type="danger" plain style="width: 100%"
              >删除节点</el-button
            >
          </template>
        </el-popconfirm>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, reactive } from "vue";
import { useGraphStore } from "@/stores/graphStore";

const props = defineProps({
  currentNode: { type: Object, default: null },
});
const emit = defineEmits(["update:currentNode"]); // 如果需要清空选中

const store = useGraphStore();
const isEditing = ref(false);

const categoryOptions = [
  { value: "Pathogen & Disease", label: "病原与病害 (Pathogen & Disease)" },
  { value: "Biocontrol", label: "生物防治 (Biocontrol)" },
  { value: "Vector Insect", label: "媒介昆虫 (Vector Insect)" },
  { value: "Host Plant", label: "寄主植物 (Host Plant)" },
  { value: "Symptom", label: "症状表现 (Symptom)" },
  { value: "Disease Feature", label: "病害特征 (Disease Feature)" },
  { value: "Risk Assessment", label: "风险评估 (Risk Assessment)" },
  {
    value: "Environment & Risk Factor",
    label: "环境与风险因子 (Environment & Risk Factor)",
  },
  { value: "Assessment Result", label: "评估结果 (Assessment Result)" },
  { value: "Prevention Method", label: "防治措施 (Prevention Method)" },
];

// 编辑表单数据副本
const editForm = reactive({
  id: null,
  name: "",
  cnName: "",
  category: "",
  description: "",
});

// 监听选中节点变化，重置状态
watch(
  () => props.currentNode,
  (newVal) => {
    isEditing.value = false;
    if (newVal) {
      // 填充表单
      editForm.id = newVal.id;
      editForm.name = newVal.name;
      editForm.cnName = newVal.cn_name || newVal.cnName; // 兼容不同字段名
      editForm.category = newVal.category;
      editForm.description = newVal.description;
    }
  },
  { deep: true }
);

// B1: 保存修改
const handleSave = async () => {
  try {
    const updated = await store.updateNodeAction(editForm);
    isEditing.value = false;
    // 注意：Store更新了，currentNode 是引用的 Store 中的对象，所以也会自动更新
    // 如果没有自动更新，可以在这里 emit 新对象回父组件
  } catch (e) {
    // error handled in store
  }
};

// B2: 删除节点
const handleDelete = async () => {
  if (!props.currentNode) return;
  try {
    await store.deleteNodeAction(props.currentNode.id);
    // 触发父组件清空选中状态 (需要在父组件监听 @clear-selection 或类似逻辑)
    // 这里简单处理：修改父组件传来的对象为null是不行的，需要通过事件
    // 为了简单，我们直接刷新或依赖父组件的响应式
    window.location.reload(); // 最粗暴的刷新，建议在父组件处理 currentNode = null
  } catch (e) {
    // error
  }
};
</script>

<style scoped>
.info-panel-container {
  width: 320px;
  height: 100%;
  border-left: 1px solid #dcdfe6;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.info-card {
  height: 100%;
  border: none;
  display: flex;
  flex-direction: column;
}
:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止溢出 */
  padding: 15px;
}
.panel-content {
  flex: 1;
  overflow-y: auto;
}
.panel-footer {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>

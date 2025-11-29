<template>
  <el-dialog
      v-model="visible"
      title="松材线虫病入侵风险智能评估"
      width="700px"
      destroy-on-close
      :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="130px" size="default">

      <!-- ================= I1: 媒介昆虫 (Vector) ================= -->
      <el-divider content-position="left">
        <el-icon><Connection /></el-icon> 媒介昆虫因素 (I1)
      </el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="媒介种类(I11)">
            <el-select v-model="form.vectorCount" placeholder="种类数量">
              <el-option label="≥3种" :value="3" />
              <el-option label="2种" :value="2" />
              <el-option label="1种" :value="1" />
              <el-option label="无" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生活史(I12)">
            <el-select v-model="form.lifeHistory" placeholder="生活史代数">
              <el-option label="1年2代以上" :value="3" />
              <el-option label="1年1代" :value="2" />
              <el-option label="2-3年1代" :value="1" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="分布面积(I13)">
            <el-select v-model="form.vectorDistribution" placeholder="媒介分布">
              <el-option label="广 (全分布)" :value="3" />
              <el-option label="中 (局部)" :value="2" />
              <el-option label="小 (零星)" :value="1" />
              <el-option label="无分布" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="携带能力(I14)">
            <el-select v-model="form.vectorCapacity" placeholder="携带线虫能力">
              <el-option label="携带率大，携带能力强" :value="3" />
              <el-option label="携带率较大，携带能力较强" :value="2" />
              <el-option label="携带率小，携带能力弱" :value="1" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- ================= I2: 寄主植物 (Host) ================= -->
      <el-divider content-position="left">
        <el-icon><HomeFilled /></el-icon> 寄主植物因素 (I2)
      </el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="寄主分布(I21)">
            <el-select v-model="form.hostDistribution" placeholder="寄主分布范围">
              <el-option label="广" :value="3" />
              <el-option label="中等" :value="2" />
              <el-option label="小" :value="1" />
              <el-option label="无分布" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="感病性(I22)">
            <el-select v-model="form.hostSusceptibility" placeholder="易感程度">
              <el-option label="容易感病" :value="3" />
              <el-option label="较易感病" :value="2" />
              <el-option label="不易/自然界能感病" :value="1" />
              <el-option label="不感病" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="经济价值(I23)">
        <el-radio-group v-model="form.hostValue">
          <el-radio :label="3">高</el-radio>
          <el-radio :label="2">一般</el-radio>
          <el-radio :label="1">低</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- ================= I3 & I4: 环境因素 ================= -->
      <el-divider content-position="left">
        <el-icon><Sunny /></el-icon> 环境气象因素 (I3 & I4)
      </el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年均降雨(mm)">
            <!-- I31 -->
            <el-input-number v-model="form.annualRainfall" :min="0" :step="100" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="6-8月降雨(mm)">
            <!-- I32 -->
            <el-input-number v-model="form.summerRainfall" :min="0" :step="50" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年均气温(℃)">
            <!-- I41 -->
            <el-input-number v-model="form.annualTemp" :step="0.5" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="6-8月均温(℃)">
            <!-- I42 -->
            <el-input-number v-model="form.summerTemp" :step="0.5" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取 消</el-button>
        <el-button type="primary" @click="handleAssess" :loading="loading">
          开始计算 (Calculate)
        </el-button>
      </span>
    </template>

    <!-- 结果展示弹窗 -->
    <el-dialog v-model="resultVisible" width="30%" title="评估报告" append-to-body center>
      <div class="result-container">
        <el-progress type="dashboard" :percentage="scorePercentage" :color="colors" :stroke-width="10" />
        <h2 :style="{ color: riskColor }">{{ result.riskLevel }}</h2>
        <div class="score-detail">
          <p><strong>综合风险指数 (D₀):</strong> {{ result.score?.toFixed(3) }}</p>
          <p class="advice-text">{{ result.advice }}</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="resultVisible = false">确 定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
// 确保你的 api 文件里导出了 assessRisk
import { assessRisk } from '@/api/diagnosis'
// 如果需要图标，记得引入
import { Connection, HomeFilled, Sunny } from '@element-plus/icons-vue'

const visible = ref(false)
const resultVisible = ref(false)
const loading = ref(false)
const result = ref({})

// 表单数据：完整对应 11 个指标
const form = reactive({
  // I1: 媒介
  vectorCount: 1,        // I11
  lifeHistory: 2,        // I12
  vectorDistribution: 2, // I13
  vectorCapacity: 2,     // I14

  // I2: 寄主
  hostDistribution: 2,   // I21
  hostSusceptibility: 2, // I22
  hostValue: 2,          // I23

  // I3: 降雨 (数值，后端负责判断区间)
  annualRainfall: 1600,  // I31
  summerRainfall: 500,   // I32

  // I4: 温度 (数值)
  annualTemp: 16.0,      // I41
  summerTemp: 26.0       // I42
})

// 进度条百分比 (D0满分是3分，映射到100%)
const scorePercentage = computed(() => {
  if (!result.value.score) return 0
  return Math.min((result.value.score / 3.0) * 100, 100)
})

// 风险颜色动态变化
const riskColor = computed(() => {
  const score = result.value.score || 0
  if (score >= 2.0) return '#F56C6C' // 红
  if (score >= 1.0) return '#E6A23C' // 黄
  return '#67C23A' // 绿
})

const colors = [
  { color: '#67C23A', percentage: 33 },
  { color: '#E6A23C', percentage: 66 },
  { color: '#F56C6C', percentage: 100 },
]

const handleAssess = async () => {
  loading.value = true
  try {
    // 模拟调用后端 (如果你后端还没跑起来，可以取消下面注释测试UI)
    // await new Promise(r => setTimeout(r, 1000))
    // result.value = { score: 2.15, riskLevel: '高风险', advice: '建议立即封锁疫区并进行疫木清理。' }

    const res = await assessRisk(form)
    result.value = res
    resultVisible.value = true
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

defineExpose({ open: () => visible.value = true })
</script>

<style scoped>
.result-container {
  text-align: center;
  padding: 10px 0;
}
.score-detail {
  margin-top: 20px;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
}
.advice-text {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}
/* 给表单项加点底部间距，防止挤在一起 */
.el-form-item {
  margin-bottom: 22px;
}
</style>
<template>
  <!-- 骨架草稿 · 由 D 负责完善 · 数据源 7.2 /reports/* 与视图 v_project_progress -->
  <div class="page">
    <div class="page__head">
      <h2>总览</h2>
      <span class="page__hint">骨架草稿 · 由 D 负责完善 · 数据源 7.2 /reports/*</span>
    </div>

    <el-form inline class="page__filter" @submit.prevent>
      <el-form-item label="项目">
        <!-- TODO(D): options 待接 GET /projects -->
        <el-select v-model="query.projectId" placeholder="全部项目" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="统计周期">
        <!-- TODO(D): 换成 el-date-picker 的 daterange -->
        <el-input v-model="query.range" placeholder="如 2026-10-01 ~ 2026-10-31" style="width: 220px" />
      </el-form-item>
      <el-button type="primary" @click="notImplemented('查询')">查询</el-button>
    </el-form>

    <!-- 指标卡：数值全部占位 0，口径待 D 与 7.2 对齐 -->
    <el-row :gutter="12">
      <el-col v-for="card in STAT_CARDS" :key="card.key" :xs="12" :sm="12" :md="6">
        <el-card shadow="never" class="page__stat">
          <el-statistic :title="card.title" :value="0" />
          <span class="page__stat-note">{{ card.note }}</span>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区：结构留位，图表由 D 用 ECharts 实现 -->
    <el-row :gutter="12">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="page__chart">
          <template #header>项目进度</template>
          <el-empty description="待 D 实现（GET /reports/project-progress，视图 v_project_progress）" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="page__chart">
          <template #header>成员工作量</template>
          <el-empty description="待 D 实现（GET /reports/member-workload）" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// 骨架草稿：只给结构与接线点，指标口径 / 图表 / 样式版面由 D 完善。
// TODO(D): import { getProjectProgress, getMemberWorkload, getDueThisWeek } from '@/api/report'
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const STAT_CARDS = [
  { key: 'course', title: '进行中课程', note: '口径待定' },
  { key: 'project', title: '进行中项目', note: '口径待定' },
  { key: 'taskDue', title: '本周到期任务', note: 'GET /reports/due-this-week' },
  { key: 'overdue', title: '逾期任务', note: '口径待定' }
]

const query = reactive({ projectId: '', range: '' })

function notImplemented(action) {
  ElMessage.info(`草稿页：${action}待接入接口`)
}
</script>

<style scoped>
/* 只留结构定位，配色 / 间距 / 响应式由成员定稿 */
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page__head {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.page__head h2 {
  margin: 0;
  font-size: var(--tp-font-size-large);
}

.page__hint {
  font-size: var(--tp-font-size-small);
  color: var(--tp-text-secondary);
}

.page__filter {
  margin-bottom: -8px;
}

.page__stat {
  margin-bottom: 12px;
}

.page__stat-note {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--tp-text-secondary);
}

.page__chart {
  min-height: 280px;
  margin-bottom: 12px;
}
</style>

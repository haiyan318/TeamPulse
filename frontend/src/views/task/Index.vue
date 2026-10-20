<template>
  <!-- 骨架草稿 · 由 C 负责完善 · 接口见 7.2 /tasks 与 /tasks/board -->
  <div class="page">
    <div class="page__head">
      <h2>任务管理</h2>
      <span class="page__hint">骨架草稿 · 由 C 负责完善 · 接口见 7.2 /tasks、/tasks/board</span>
    </div>

    <el-form inline class="page__filter" @submit.prevent>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="任务名称 / 编号" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-select v-model="query.priority" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="item in PRIORITY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form>

    <div class="page__toolbar">
      <!-- TODO(C): 看板拖拽与新建任务 -->
      <el-radio-group v-model="viewMode" size="default">
        <el-radio-button value="list">列表</el-radio-button>
        <el-radio-button value="board">看板</el-radio-button>
      </el-radio-group>
      <el-button type="primary" plain @click="notImplemented('新建任务')">新建任务</el-button>
    </div>

    <template v-if="viewMode === 'list'">
      <!-- 列 prop 为占位（取自 schema.sql 的 task 表），以 7.2 的 VO 字段为准 -->
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="taskCode" label="编号" width="120" />
        <el-table-column prop="taskName" label="任务名称" min-width="200" />
        <el-table-column prop="groupName" label="所属小组" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">{{ statusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">{{ priorityLabel(row.priority) }}</template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="110" />
        <el-table-column prop="planEndTime" label="计划截止" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default>
            <el-button link type="primary" @click="notImplemented('编辑')">编辑</el-button>
            <el-button link type="primary" @click="notImplemented('流转状态')">流转</el-button>
            <el-button link type="danger" @click="notImplemented('删除')">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>暂无数据（草稿：数据源待接 GET /tasks）</template>
      </el-table>

      <el-pagination
        class="page__pager"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        @current-change="handleSearch"
        @size-change="handleSearch"
      />
    </template>

    <!-- 看板视图整体留给 C：按状态分列 + 拖拽改状态（PUT /tasks/{id}/status） -->
    <el-card v-else shadow="never" class="page__board">
      <el-empty description="看板视图待 C 实现（数据源：GET /tasks/board）" />
    </el-card>
  </div>
</template>

<script setup>
// 骨架草稿：只给结构与接线点，业务逻辑 / 样式版面由 C 完善。
// TODO(C): import { getTasks, getTaskBoard, createTask, updateTask, updateTaskStatus, deleteTask } from '@/api/task'
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

// 状态 / 优先级取值来自 README「接口约定」，不可自行改码
const STATUS_OPTIONS = [
  { value: 'NOT_STARTED', label: '未开始' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'REVIEWING', label: '评审中' },
  { value: 'DONE', label: '已完成' },
  { value: 'PAUSED', label: '已暂停' },
  { value: 'CANCELLED', label: '已取消' }
]
const PRIORITY_OPTIONS = [
  { value: 1, label: '高' },
  { value: 2, label: '中' },
  { value: 3, label: '低' }
]

const loading = ref(false)
const list = ref([])
const total = ref(0)
const viewMode = ref('list')
const query = reactive({ keyword: '', status: '', priority: '', pageNum: 1, pageSize: 10 })

function statusLabel(value) {
  return STATUS_OPTIONS.find((item) => item.value === value)?.label || '—'
}

function priorityLabel(value) {
  return PRIORITY_OPTIONS.find((item) => item.value === value)?.label || '—'
}

// TODO(C): const data = await getTasks(query); list.value = data.list; total.value = data.total
function handleSearch() {
  notImplemented('查询')
}

function handleReset() {
  Object.assign(query, { keyword: '', status: '', priority: '', pageNum: 1 })
  handleSearch()
}

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

.page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page__board {
  min-height: 320px;
}

.page__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>

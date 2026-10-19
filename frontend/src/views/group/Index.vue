<template>
  <!-- 骨架草稿 · 由 B 负责完善 · 接口见 7.2 /groups -->
  <div class="page">
    <div class="page__head">
      <h2>小组管理</h2>
      <span class="page__hint">骨架草稿 · 由 B 负责完善 · 接口见 7.2 /groups</span>
    </div>

    <el-form inline class="page__filter" @submit.prevent>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="小组名称" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="所属项目">
        <el-input v-model="query.projectId" placeholder="项目 ID（后续换下拉）" clearable style="width: 180px" />
      </el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form>

    <div class="page__toolbar">
      <!-- TODO(B): 新建小组弹窗 + 维护成员（POST /groups/{id}/members） -->
      <el-button type="primary" plain @click="notImplemented('新建小组')">新建小组</el-button>
      <el-button @click="notImplemented('批量维护成员')">维护成员</el-button>
    </div>

    <!-- 列 prop 为占位（取自 schema.sql 的 project_group / group_member），以 7.2 的 VO 为准 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="groupName" label="小组名称" min-width="160" />
      <el-table-column prop="projectName" label="所属项目" min-width="160" />
      <el-table-column prop="leaderName" label="组长" width="120" />
      <el-table-column prop="memberCount" label="人数" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">{{ row.status === 1 ? '正常' : '已解散' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default>
          <el-button link type="primary" @click="notImplemented('成员管理')">成员</el-button>
          <el-button link type="primary" @click="notImplemented('设置组长')">设组长</el-button>
          <el-button link type="danger" @click="notImplemented('解散小组')">解散</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无数据（草稿：数据源待接 GET /groups）</template>
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
  </div>
</template>

<script setup>
// 骨架草稿：只给结构与接线点，业务逻辑 / 样式版面由 B 完善。
// TODO(B): import { getGroups, createGroup, deleteGroup, addGroupMembers, removeGroupMembers, updateGroupLeader } from '@/api/group'
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', projectId: '', pageNum: 1, pageSize: 10 })

// TODO(B): const data = await getGroups(query); list.value = data.list; total.value = data.total
function handleSearch() {
  notImplemented('查询')
}

function handleReset() {
  Object.assign(query, { keyword: '', projectId: '', pageNum: 1 })
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

.page__pager {
  justify-content: flex-end;
}
</style>

<template>
  <!-- 骨架草稿 · 由 B 负责完善 · 接口见 7.2 /courses -->
  <div class="page">
    <div class="page__head">
      <h2>课程管理</h2>
      <span class="page__hint">骨架草稿 · 由 B 负责完善 · 接口见 7.2 /courses</span>
    </div>

    <!-- 查询区：字段名待与 7.2 对齐，版面细节由成员定 -->
    <el-form inline class="page__filter" @submit.prevent>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="课程名 / 课程代码" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="学期">
        <el-input v-model="query.semester" placeholder="如 2026-2027-1" clearable style="width: 170px" />
      </el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form>

    <div class="page__toolbar">
      <!-- TODO(B): 换成新建课程弹窗 -->
      <el-button type="primary" plain @click="notImplemented('新建课程')">新建课程</el-button>
    </div>

    <!-- 列 prop 为占位（取自 schema.sql 的 course 表），以 7.2 的 VO 字段为准，接接口时校正 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="courseCode" label="课程代码" width="140" />
      <el-table-column prop="courseName" label="课程名称" min-width="180" />
      <el-table-column prop="semester" label="学期" width="150" />
      <el-table-column prop="teacherName" label="任课教师" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">{{ row.status === 1 ? '进行中' : '已结课' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default>
          <el-button link type="primary" @click="notImplemented('编辑')">编辑</el-button>
          <el-button link type="danger" @click="notImplemented('删除')">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无数据（草稿：数据源待接 GET /courses）</template>
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
// 刻意不发请求：后端 /courses 尚未实现，发了只会得到 404 噪音。
// TODO(B): import { getCourses, createCourse, updateCourse, deleteCourse } from '@/api/course'
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
// 分页字段用 7.3 冻结口径 pageNum / pageSize
const query = reactive({ keyword: '', semester: '', pageNum: 1, pageSize: 10 })

// TODO(B): const data = await getCourses(query); list.value = data.list; total.value = data.total
function handleSearch() {
  notImplemented('查询')
}

function handleReset() {
  Object.assign(query, { keyword: '', semester: '', pageNum: 1 })
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

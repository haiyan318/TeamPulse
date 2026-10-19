<template>
  <el-container class="layout">
    <el-aside
      class="layout__aside"
      :width="isCollapse ? 'var(--tp-sidebar-collapsed-width)' : 'var(--tp-sidebar-width)'"
    >
      <div class="layout__logo">
        <span class="layout__logo-mark">TP</span>
        <span v-show="!isCollapse" class="layout__logo-text">TeamPulse</span>
      </div>

      <!-- router 模式：菜单项 index 即路由 path，点击直接跳转 -->
      <el-menu
        class="layout__menu"
        :default-active="route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
      >
        <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">
          <!-- 图标名来自 menu.js 的 meta.icon，靠 main.js 的全局注册解析 -->
          <el-icon><component :is="item.meta.icon" /></el-icon>
          <template #title>{{ item.meta.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout__header">
        <div class="layout__header-left">
          <el-button text :icon="isCollapse ? Expand : Fold" @click="isCollapse = !isCollapse" />
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-dropdown trigger="click" @command="handleCommand">
          <span class="layout__user">
            <el-avatar :size="26">{{ avatarText }}</el-avatar>
            <span class="layout__user-name">{{ userStore.displayName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="layout__main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Expand, Fold } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { menuRoutes } from '@/router/menu'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

/** 按角色过滤菜单：meta.roles 缺省表示所有登录用户可见 */
const visibleMenus = computed(() =>
  menuRoutes.filter((item) => !item.meta?.roles || userStore.hasAnyRole(item.meta.roles))
)

const currentTitle = computed(() => (route.path === '/dashboard' ? '' : route.meta?.title || ''))
const avatarText = computed(() => userStore.displayName.slice(0, 1))

async function handleCommand(command) {
  if (command !== 'logout') return
  // 先通知后端（契约占位，失败也不阻断），再清本地并回到登录页
  await userStore.logoutRemote()
  router.replace('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.layout__aside {
  display: flex;
  flex-direction: column;
  background-color: var(--tp-bg-container);
  border-right: 1px solid var(--tp-border-color);
  transition: width 0.2s;
}

.layout__logo {
  display: flex;
  align-items: center;
  gap: 8px;
  height: var(--tp-header-height);
  padding: 0 16px;
  overflow: hidden;
  white-space: nowrap;
  border-bottom: 1px solid var(--tp-border-color);
}

.layout__logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-weight: 600;
  color: #fff;
  background-color: var(--tp-color-primary);
  border-radius: var(--tp-radius-base);
}

.layout__logo-text {
  font-size: var(--tp-font-size-large);
  font-weight: 600;
}

/* 避免与 aside 的右边框叠成双线 */
.layout__menu {
  flex: 1;
  border-right: none;
}

.layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--tp-header-height);
  background-color: var(--tp-bg-container);
  border-bottom: 1px solid var(--tp-border-color);
}

.layout__header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.layout__user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.layout__user-name {
  font-size: var(--tp-font-size-base);
  color: var(--tp-text-regular);
}

.layout__main {
  padding: var(--tp-content-padding);
  background-color: var(--tp-bg-page);
}
</style>

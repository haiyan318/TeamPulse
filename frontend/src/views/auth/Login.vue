<template>
  <div class="login">
    <el-card class="login__card" shadow="always">
      <div class="login__head">
        <h1 class="login__title">TeamPulse</h1>
        <p class="login__subtitle">课程小组项目进度与贡献分析</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model.trim="form.username"
            placeholder="请输入用户名"
            autocomplete="username"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            show-password
            @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <!-- 业务失败（如 1002 用户名或密码错误）在此红字提示；login 走 silent，不会再弹 toast -->
        <p v-if="errorText" class="login__error">{{ errorText }}</p>

        <el-button
          type="primary"
          class="login__submit"
          :loading="loading"
          @click="handleSubmit"
        >
          登录
        </el-button>
      </el-form>

      <p class="login__hint">账号由管理员分配，如需开通请联系任课教师或助教。</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const errorText = ref('')

const form = reactive({ username: '', password: '' })

// 只校验非空：后端 LoginRequest 也只有 @NotBlank，加长度规则会把历史短密码挡在门外
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/** 只接受站内绝对路径，挡住 //evil.com 这类开放重定向 */
function safeRedirect(target) {
  if (typeof target !== 'string') return '/dashboard'
  if (!target.startsWith('/') || target.startsWith('//')) return '/dashboard'
  return target
}

async function handleSubmit() {
  if (loading.value) return
  errorText.value = ''

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({ username: form.username, password: form.password })
    // 按 6.5 第 2 步：登录成功后再拉一次 /auth/me，角色以服务端为准
    try {
      await userStore.fetchMe()
    } catch {
      // 非致命：roles 已由登录响应带出，失败不阻断跳转
    }
    await router.replace(safeRedirect(route.query.redirect))
  } catch (err) {
    errorText.value = err?.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background-color: var(--tp-bg-page);
}

.login__card {
  width: 100%;
  max-width: 400px;
  border-radius: var(--tp-radius-lg);
  box-shadow: var(--tp-shadow-base);
}

.login__head {
  margin-bottom: 20px;
  text-align: center;
}

.login__title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--tp-text-primary);
}

.login__subtitle {
  margin: 8px 0 0;
  font-size: var(--tp-font-size-small);
  color: var(--tp-text-secondary);
}

.login__error {
  margin: -4px 0 12px;
  font-size: var(--tp-font-size-small);
  color: var(--el-color-danger);
}

.login__submit {
  width: 100%;
}

.login__hint {
  margin: 16px 0 0;
  font-size: 12px;
  color: var(--tp-text-secondary);
  text-align: center;
}
</style>

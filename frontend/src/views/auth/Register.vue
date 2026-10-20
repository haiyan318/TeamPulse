<template>
  <div class="register">
    <el-card class="register__card" shadow="always">
      <div class="register__head">
        <h1 class="register__title">注册 TeamPulse</h1>
        <p class="register__subtitle">注册后默认获得「学生」角色</p>
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
            maxlength="50"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="至少 6 位"
            autocomplete="new-password"
            show-password
            maxlength="64"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            autocomplete="new-password"
            show-password
            maxlength="64"
            @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-form-item label="姓名（可选）" prop="realName">
          <el-input v-model.trim="form.realName" placeholder="真实姓名" maxlength="50" />
        </el-form-item>

        <el-form-item label="邮箱（可选）" prop="email">
          <el-input
            v-model.trim="form.email"
            placeholder="name@example.com"
            autocomplete="email"
            maxlength="100"
            @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <el-button
          type="primary"
          class="register__submit"
          :loading="loading"
          @click="handleSubmit"
        >
          注册
        </el-button>
      </el-form>

      <p class="register__foot">
        已有账号？
        <el-link type="primary" underline="never" @click="goLogin">返回登录</el-link>
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: ''
})

/** 两次密码一致：async-validator 拿不到另一个字段，只能写成闭包函数 */
function validateConfirm(rule, value, callback) {
  if (!value) return callback(new Error('请再次输入密码'))
  if (value !== form.password) return callback(new Error('两次输入的密码不一致'))
  return callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    // 对齐 sys_user.username VARCHAR(50)：后端 DTO 没有长度约束，超长会落成 500，前端先挡住
    { max: 50, message: '用户名不能超过 50 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    // 下限对齐后端 @Size(min = 6)；上限 64 规避 BCrypt 72 字节截断
    { min: 6, max: 64, message: '密码长度需为 6-64 位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
  realName: [{ max: 50, message: '姓名不能超过 50 位', trigger: 'blur' }],
  // 后端 @Email 允许空值，这里同样只在有值时校验
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }]
}

// 先填确认密码、再回头改密码时，主动刷新确认框的校验态（提交时仍会整体校验）
watch(
  () => form.password,
  () => {
    if (!form.confirmPassword) return
    formRef.value?.validateField('confirmPassword', () => {})
  }
)

async function handleSubmit() {
  if (loading.value) return

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 可选字段为空时不发送，避免把空串写进库
    const payload = { username: form.username, password: form.password }
    if (form.realName) payload.realName = form.realName
    if (form.email) payload.email = form.email

    await register(payload)

    // 失败提示由 request.js 统一 toast（本接口未加 silent），此处不再重复红字
    ElMessage.success('注册成功，请登录')
    await router.replace({ path: '/login', query: { username: form.username } })
  } catch {
    // 409（用户名已存在）/ 400（校验失败）的 message 已被拦截器弹出，这里保持静默
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.replace('/login')
}
</script>

<style scoped>
.register {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background-color: var(--tp-bg-page);
}

.register__card {
  width: 100%;
  max-width: 400px;
  border-radius: var(--tp-radius-lg);
  box-shadow: var(--tp-shadow-base);
}

.register__head {
  margin-bottom: 20px;
  text-align: center;
}

.register__title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--tp-text-primary);
}

.register__subtitle {
  margin: 8px 0 0;
  font-size: var(--tp-font-size-small);
  color: var(--tp-text-secondary);
}

.register__submit {
  width: 100%;
}

.register__foot {
  margin: 16px 0 0;
  font-size: 12px;
  color: var(--tp-text-secondary);
  text-align: center;
}

/* 窗口较矮时顶到可滚动位置，避免卡片被裁掉顶部 */
@media (max-height: 700px) {
  .register {
    align-items: flex-start;
  }
}
</style>

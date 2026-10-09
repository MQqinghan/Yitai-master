<template>
  <!-- PC端：外层容器 + 登录弹窗 -->
  <div class="login-page pc-login" v-if="isPc">
    <el-dialog
        v-model="dialogVisible"
        title=""
        width="400px"
        :close-on-click-modal="false"
        class="login-dialog"
        :show-close="false"
    >
      <template #header>
        <div class="dialog-header">
          <span class="welcome-title">欢迎回来！</span>
          <el-icon class="close-icon" @click="handleClose">
            <Close />
          </el-icon>
        </div>
      </template>
      <div class="login-form">
        <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            class="login-input"
        />
        <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            show-password
            class="login-input"
        />
        <el-button type="primary" class="login-btn" @click="handleLogin">
          登录
        </el-button>
        <div class="forgot-pwd" @click="handleForgotPwd">
          忘记密码？
        </div>
      </div>
      <template #footer>
        <div class="register-tip">
          还没有账号？
          <span class="register-link" @click="handleRegister">马上注册</span>
        </div>
      </template>
    </el-dialog>
  </div>

  <!-- 移动端：占满屏幕的单独页面 -->
  <div class="login-page mobile-login" v-else>
    <div class="mobile-header">
      <el-icon class="back-icon" @click="handleBack">
        <ArrowLeft />
      </el-icon>
    </div>
    <div class="mobile-content">
      <h2 class="welcome-title">欢迎回来！</h2>
      <el-input
          v-model="loginForm.username"
          placeholder="用户名"
          class="login-input"
      />
      <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="密码"
          show-password
          class="login-input"
      />
      <el-button type="primary" class="login-btn" @click="handleLogin">
        登录
      </el-button>
      <div class="forgot-pwd" @click="handleForgotPwd">
        忘记密码？
      </div>
      <div class="register-tip">
        还没有账号？
        <span class="register-link" @click="handleRegister">马上注册</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { login } from '@/api/user'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Close } from '@element-plus/icons-vue'
import { useDevice } from '@/hooks/useDevice'
import { useGoHome } from '@/hooks/useGoHome'
import { loadUserInfo } from '@/hooks/useUser'

const router = useRouter()
const { isPc } = useDevice()
// 跳转首页
const { goHome } = useGoHome()
// PC端弹窗显示状态
const dialogVisible = ref(true)
// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})



// 关闭弹窗（PC端）
const handleClose = () => {
  dialogVisible.value = false
  goHome()
}

// 返回上一页（移动端）
const handleBack = () => {
  goHome()
}

// 登录逻辑
const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  try {
    const res = await login(loginForm.value)
    console.log('登录成功，token：', res.data)
    
    // 存 token
    localStorage.setItem('token', res.data)
    
    // 加载用户信息
    await loadUserInfo()
    
    ElMessage.success('登录成功')
    
    // 跳首页
    goHome()
  } catch (error: any) {
    console.error('登录失败', error)
    // 后端返回的错误信息通常在 error.response.data.message 或 error.data.message
    const errorMsg = error.response?.data?.message || error.data?.message || '登录失败，请检查用户名和密码'
    ElMessage.error(errorMsg)
  }
}

// 忘记密码
const handleForgotPwd = () => {
  ElMessage.info('忘记密码功能开发中')
}

// 注册跳转
const handleRegister = () => {
  router.push('/register')
}

onMounted(() => {
  // PC端默认显示弹窗
  if (isPc.value) {
    dialogVisible.value = true
  }
})
</script>

<style scoped>
@import '@/assets/css/login.css';
</style>
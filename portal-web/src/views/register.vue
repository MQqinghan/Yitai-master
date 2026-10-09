<template>
  <!-- PC端：弹窗形式 -->
  <div class="register-page pc-register" v-if="isPc">
    <el-dialog
        v-model="dialogVisible"
        title=""
        width="500px"
        :close-on-click-modal="false"
        class="register-dialog"
        :show-close="false"
    >
      <template #header>
        <div class="dialog-header">
          <span class="welcome-title">欢迎注册</span>
          <el-icon class="close-icon" @click="handleClose">
            <Close />
          </el-icon>
        </div>
      </template>
      <div class="register-form">
        <el-input
            v-model="registerForm.username"
            placeholder="请输入手机号或邮箱"
            class="register-input"
        >
          <template #prepend>用户名</template>
        </el-input>

        <el-input
            v-model="registerForm.code"
            placeholder="请输入验证码"
            class="register-input"
        >
          <template #prepend>验证码</template>
          <template #append>
            <el-button
                class="code-btn"
                :disabled="countdown > 0"
                @click="handleGetCode"
            >
              {{ countdown > 0 ? `${countdown}s后重新获取` : '获取验证码' }}
            </el-button>
          </template>
        </el-input>

        <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="6-18位不含特殊字符的数字、字母组合"
            show-password
            class="register-input"
        >
          <template #prepend>密码</template>
        </el-input>

        <el-input
            v-model="registerForm.confirmPwd"
            type="password"
            placeholder="6-18位不含特殊字符的数字、字母组合"
            show-password
            class="register-input"
        >
          <template #prepend>确认密码</template>
        </el-input>

        <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            class="register-input"
        >
          <template #prepend>昵称</template>
        </el-input>

        <el-button type="primary" class="register-btn" @click="handleRegister">
          注册
        </el-button>
      </div>
      <template #footer>
        <div class="login-tip">
          已有账号？
          <span class="login-link" @click="handleGoLogin">马上登录</span>
        </div>
      </template>
    </el-dialog>
  </div>

  <!-- 移动端：占满屏幕的单独页面 -->
  <div class="register-page mobile-register" v-else>
    <div class="mobile-header">
      <el-icon class="back-icon" @click="handleBack">
        <ArrowLeft />
      </el-icon>
    </div>
    <div class="mobile-content">
      <h2 class="welcome-title">欢迎注册</h2>
      <el-input
          v-model="registerForm.username"
          placeholder="请输入手机号或邮箱"
          class="register-input"
      >
        <template #prepend>用户名</template>
      </el-input>

      <el-input
          v-model="registerForm.code"
          placeholder="请输入验证码"
          class="register-input"
      >
        <template #prepend>验证码</template>
        <template #append>
          <el-button
              class="code-btn"
              :disabled="countdown > 0"
              @click="handleGetCode"
          >
            {{ countdown > 0 ? `${countdown}s后重新获取` : '获取验证码' }}
          </el-button>
        </template>
      </el-input>

      <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="6-18位不含特殊字符的数字、字母组合"
          show-password
          class="register-input"
      >
        <template #prepend>密码</template>
      </el-input>

      <el-input
          v-model="registerForm.confirmPwd"
          type="password"
          placeholder="6-18位不含特殊字符的数字、字母组合"
          show-password
          class="register-input"
      >
        <template #prepend>确认密码</template>
      </el-input>

      <el-input
          v-model="registerForm.nickname"
          placeholder="请输入昵称"
          class="register-input"
      >
        <template #prepend>昵称</template>
      </el-input>

      <el-button type="primary" class="register-btn" @click="handleRegister">
        注册
      </el-button>

      <div class="login-tip">
        已有账号？
        <span class="login-link" @click="handleGoLogin">马上登录</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { register, sendSmsCode } from '@/api/user'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Close } from '@element-plus/icons-vue'
import { useDevice } from '@/hooks/useDevice'

const router = useRouter()
const { isPc } = useDevice()


// PC端弹窗显示状态
const dialogVisible = ref(true)

// 注册表单
const registerForm = ref({
  username: '',
  code: '',
  password: '',
  confirmPwd: '',
  nickname: ''
})

// 验证码倒计时
const countdown = ref(0)
let timer: NodeJS.Timeout | null = null

// 关闭弹窗（PC 端）
const handleClose = () => {
  dialogVisible.value = false
  router.back()
}

// 返回上一页（移动端）
const handleBack = () => {
  router.back()
}

// 获取验证码
const handleGetCode = async () => {
  if (!registerForm.value.username) {
    ElMessage.warning('请先输入用户名（手机号/邮箱）')
    return
  }
  
  try {
    // 调用后端发送验证码接口
    await sendSmsCode(registerForm.value.username)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer!)
        timer = null
      }
    }, 1000)
  } catch (error: any) {
    console.error('验证码发送失败', error)
    ElMessage.error(error.response?.data?.message || '验证码发送失败，请重试')
  }
}

const handleRegister = async () => {
  const { username, code, password, confirmPwd, nickname } = registerForm.value
  
  if (!username || !code || !password || !confirmPwd || !nickname) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (password !== confirmPwd) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  
  try {
    await register({
      username,
      password,
      nickName: nickname,
      code
    })
    ElMessage.success('注册成功')
    handleGoLogin()
  } catch (error: any) {
    console.error('注册失败', error)
    const errorMsg = error.response?.data?.message || error.data?.message || '注册失败，请重试'
    ElMessage.error(errorMsg)
  }
}

// 跳转登录
const handleGoLogin = () => {
  if (isPc.value) {
    dialogVisible.value = false
  }
  router.push('/login')
}

onMounted(() => {
  if (isPc.value) {
    dialogVisible.value = true
  }
})
</script>

<style scoped>
@import '@/assets/css/register.css';
</style>
<!--登录页面-->
<template>
  <div class="login">
    <el-form class="form" :model="model" :rules="rules" ref="loginForm">
      <h1 class="title">以太购物后台管理系统</h1>
      <el-form-item prop="userName">
        <el-input
          class="text"
          v-model="model.userName"
          prefix-icon="User"
          clearable
          placeholder="用户名"
        />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          class="text"
          v-model="model.password"
          prefix-icon="Lock"
          show-password
          clearable
          placeholder="密码"
        />
      </el-form-item>

      <!-- 验证码输入框+图片区域 -->
      <el-form-item prop="captcha">
        <div class="captcha">
          <el-input
            class="text"
            v-model="model.captcha"
            prefix-icon="Picture"
            placeholder="请输入验证码"
          ></el-input>
          <img :src="captchaSrc" @click="refreshCaptcha" />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button
          :loading="loading"
          type="primary"
          class="btn"
          size="large"
          @click="submit"
        >
          {{ btnText }}
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {
  defineComponent,
  getCurrentInstance,
  reactive,
  toRefs,
  ref,
  computed,
  onMounted,
} from 'vue'
// 修复：补充 GetValidateCode 导入
import { Login, GetValidateCode } from '@/api/login'
import { useRouter, useRoute } from 'vue-router'
import { useApp } from '@/pinia/modules/app'

export default defineComponent({
  name: 'login',
  setup() {
    const { proxy: ctx } = getCurrentInstance()
    const router = useRouter()
    const route = useRoute()

    // 表单校验规则（包含验证码）
    const getRules = () => ({
      userName: [
        {
          required: true,
          message: '请输入用户名',
          trigger: 'blur',
        },
      ],
      password: [
        {
          required: true,
          message: '请输入密码',
          trigger: 'blur',
        },
        {
          min: 6,
          max: 12,
          message: '密码长度6-12位',
          trigger: 'blur',
        },
      ],
      captcha: [
        {
          required: true,
          message: '请输入验证码',
          trigger: 'blur',
        },
      ],
    })

    // 登录表单状态管理
    const state = reactive({
      model: {
        userName: 'admin',
        password: '123456',
        captcha: '',
        codeKey: ''
      },
      rules: getRules(),
      loading: false,
      captchaSrc: "",
      refreshCaptcha: async () => {
        try {
          const { data } = await GetValidateCode()
          state.model.codeKey = data.codeKey
          state.captchaSrc = data.codeValue
        } catch (e) {
          ctx.$message.error('验证码加载失败，请刷新重试')
          console.error('验证码加载失败：', e)
        }
      },
      btnText: computed(() =>
        state.loading ? '登录中...' : '登录'
      ),
      loginForm: ref(null),
      submit: () => {
        if (state.loading) return
        const loginFormRef = state.loginForm
        loginFormRef.validate(async valid => {
          if (valid) {
            state.loading = true
            try {
              const { code, data, message } = await Login(state.model)
              if (+code === 200) {
                ctx.$message.success({
                  message: '登录成功',
                  duration: 1000,
                })
                const targetPath = decodeURIComponent(route.query.redirect || '/')
                if (targetPath.startsWith('http')) {
                  window.location.href = targetPath
                } else {
                  router.push(targetPath)
                }
                useApp().initToken(data)
              } else {
                state.refreshCaptcha()
                ctx.$message.error(message)
              }
            } catch (e) {
              state.refreshCaptcha()
              ctx.$message.error('登录请求失败，请重试')
              console.error('登录失败：', e)
            } finally {
              state.loading = false
            }
          }
        })
      },
    })

    // 页面加载时初始化验证码
    onMounted(() => {
      state.refreshCaptcha()
    })

    return {
      ...toRefs(state),
    }
  },
})
</script>

<style lang="scss" scoped>
.login {
  transition: transform 1s;
  transform: scale(1);
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #2d3a4b;
  .form {
    width: 520px;
    max-width: 100%;
    padding: 0 24px;
    box-sizing: border-box;
    margin: 160px auto 0;
    :deep {
      .el-input__wrapper {
        box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
        background: rgba(0, 0, 0, 0.1);
      }
      .el-input-group--append > .el-input__wrapper {
        border-top-right-radius: 0;
        border-bottom-right-radius: 0;
      }
      .el-input-group--prepend > .el-input__wrapper {
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
      }
    }
    .title {
      color: #fff;
      text-align: center;
      font-size: 24px;
      margin: 0 0 24px;
    }
    .text {
      font-size: 16px;
      :deep(.el-input__inner) {
        color: #fff;
        height: 48px;
        line-height: 48px;
        &::placeholder {
          color: rgba(255, 255, 255, 0.2);
        }
      }
    }
    .btn {
      width: 100%;
    }
    // 验证码样式（修复：缩进正确）
    .captcha {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;
      img {
        cursor: pointer;
        margin-left: 20px;
        width: 150px;
        height: 48px;
        object-fit: cover;
      }
    }
  }
}

.change-lang {
  position: fixed;
  right: 20px;
  top: 20px;
  :deep {
    .change-lang {
      height: 24px;
      &:hover {
        background: none;
      }
      .icon {
        color: #fff;
      }
    }
  }
}
</style>
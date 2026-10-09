<template>
  <div class="setting-page">
    <!-- 移动端布局 -->
    <div class="mobile-setting" v-if="!isPc">
      <!-- 顶部导航栏 -->
      <div class="mobile-header">
        <el-icon class="back-icon" @click="handleBack">
          <ArrowLeft />
        </el-icon>
        <span class="header-title">设置</span>
        <div class="header-placeholder"></div>
      </div>

      <!-- 第一组：基础设置 -->
      <div class="setting-group">
        <div class="setting-item" @click="goPage('/profile')">
          <span class="item-label">个人资料</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item" @click="goPage('/address')">
          <span class="item-label">收货地址</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item" @click="goPage('/auth')">
          <span class="item-label">实名认证</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item switch-item">
          <span class="item-label">消息推送</span>
          <el-switch v-model="messagePush" active-color="#ff4d4f" />
        </div>
        <div class="setting-item" @click="clearCache">
          <span class="item-label">清除缓存</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 第二组：账号与安全等 -->
      <div class="setting-group">
        <div class="setting-item" @click="goPage('/account-security')">
          <span class="item-label">账号与安全</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item" @click="goPage('/payment')">
          <span class="item-label">支付设置</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item" @click="goPage('/privacy')">
          <span class="item-label">隐私设置</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item" @click="goPage('/about')">
          <span class="item-label">关于</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="bottom-actions">
        <div class="action-btn" @click="switchAccount">切换账号</div>
        <div class="action-btn logout-btn" @click="handleLogout">退出登录</div>
      </div>
    </div>

    <!-- PC端布局 -->
    <div class="pc-setting" v-else>
      <div class="pc-container">
        <!-- 左侧菜单栏 -->
        <div class="pc-left-menu">
          <div class="menu-item active" @click="activeTab = 'base'">基础设置</div>
          <div class="menu-item" @click="activeTab = 'account'">账号与安全</div>
          <div class="menu-item" @click="activeTab = 'payment'">支付设置</div>
          <div class="menu-item" @click="activeTab = 'privacy'">隐私设置</div>
          <div class="menu-item" @click="activeTab = 'about'">关于</div>
        </div>

        <!-- 右侧内容区 -->
        <div class="pc-right-content">
          <!-- 基础设置 -->
          <div v-if="activeTab === 'base'" class="tab-content">
            <h3 class="tab-title">基础设置</h3>
            <div class="setting-group">
              <div class="setting-item" @click="goPage('/profile')">
                <span class="item-label">个人资料</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item" @click="goPage('/address')">
                <span class="item-label">收货地址</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item" @click="goPage('/auth')">
                <span class="item-label">实名认证</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item switch-item">
                <span class="item-label">消息推送</span>
                <el-switch v-model="messagePush" active-color="#ff4d4f" />
              </div>
              <div class="setting-item" @click="clearCache">
                <span class="item-label">清除缓存</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>

          <!-- 账号与安全 -->
          <div v-if="activeTab === 'account'" class="tab-content">
            <h3 class="tab-title">账号与安全</h3>
            <div class="setting-group">
              <div class="setting-item" @click="goPage('/change-pwd')">
                <span class="item-label">修改密码</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item" @click="goPage('/bind-phone')">
                <span class="item-label">绑定手机号</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item" @click="goPage('/auth')">
                <span class="item-label">实名认证</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>

          <!-- 支付设置 -->
          <div v-if="activeTab === 'payment'" class="tab-content">
            <h3 class="tab-title">支付设置</h3>
            <div class="setting-group">
              <div class="setting-item" @click="goPage('/payment-password')">
                <span class="item-label">支付密码</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item" @click="goPage('/payment-method')">
                <span class="item-label">支付方式管理</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>

          <!-- 隐私设置 -->
          <div v-if="activeTab === 'privacy'" class="tab-content">
            <h3 class="tab-title">隐私设置</h3>
            <div class="setting-group">
              <div class="setting-item switch-item">
                <span class="item-label">消息推送</span>
                <el-switch v-model="messagePush" active-color="#ff4d4f" />
              </div>
              <div class="setting-item" @click="goPage('/privacy-detail')">
                <span class="item-label">隐私政策</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>

          <!-- 关于 -->
          <div v-if="activeTab === 'about'" class="tab-content">
            <h3 class="tab-title">关于</h3>
            <div class="setting-group">
              <div class="setting-item" @click="goPage('/version')">
                <span class="item-label">版本信息</span>
                <span class="item-value">v1.0.0</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item" @click="goPage('/agreement')">
                <span class="item-label">用户协议</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item" @click="goPage('/privacy-policy')">
                <span class="item-label">隐私政策</span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>

          <!-- 底部操作栏 -->
          <div class="bottom-actions">
            <div class="action-btn" @click="switchAccount">切换账号</div>
            <div class="action-btn logout-btn" @click="handleLogout">退出登录</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDevice } from '@/hooks/useDevice'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { logout as logoutApi } from '@/api/user'

const router = useRouter()
const { isPc } = useDevice()

// 消息推送开关
const messagePush = ref(true)

// PC端当前激活标签
const activeTab = ref('base')

// 返回上一页
const handleBack = () => {
  router.back()
}

// 页面跳转
const goPage = (path: string) => {
  // 收货地址页面：统一使用当前页跳转（保证能正常返回）
  if (path === '/address') {
    router.push(path)
    return
  }
  
  // 其他页面正常跳转
  router.push(path)
}

// 清除缓存
const clearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清除缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 实际项目中清除本地存储/缓存
    ElMessage.success('缓存清除成功')
  } catch {
    // 用户取消
  }
}

// 切换账号
const switchAccount = () => {
  ElMessage.info('切换账号功能开发中')
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      // 调用后端退出接口
      await logoutApi()
      console.log('后端退出成功')
    } catch (error) {
      // 即使后端退出失败，也清除前端数据
      console.warn('后端退出失败，但仍清除前端数据:', error)
    }
    
    // 清除前端所有登录相关数据
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('collectList') // 清除旧的收藏缓存
    
    ElMessage.success('退出登录成功')
    
    // 跳转到登录页
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
@import '@/assets/css/setting.css';
</style>
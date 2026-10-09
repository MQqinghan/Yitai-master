<template>
  <div class="message-page">
    <!-- PC端布局 -->
    <div class="pc-layout" v-if="isPc">
      <!-- 顶部导航 -->
      <div class="message-header">
        <div class="header-left">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回</span>
          </el-button>
        </div>
        <h2 class="header-title">我的消息</h2>
        <div class="header-right">
          <el-button type="primary" size="small" @click="markAllAsRead">全部标为已读</el-button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="message-content">
        <div class="message-list">
          <div class="message-item"
               v-for="item in messageList"
               :key="item.id"
               :class="{ unread: !item.isRead }"
               @click="handleMessageClick(item)"
          >
            <div class="message-icon">
              <el-icon :size="24">
                <ChatDotRound v-if="item.type === 'system'" />
                <Bell v-else-if="item.type === 'notice'" />
                <Promotion v-else-if="item.type === 'promotion'" />
                <PriceTag v-else-if="item.type === 'price_drop'" />
                <Goods v-else-if="item.type === 'stock_alert'" />
                <ChatDotRound v-else />
              </el-icon>
            </div>
            <div class="message-info">
              <div class="message-top">
                <span class="message-type">{{ getTypeLabel(item.type) }}</span>
                <span class="message-time">{{ item.time }}</span>
              </div>
              <p class="message-title">{{ item.title }}</p>
              <p class="message-content-text">{{ item.content }}</p>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-if="!loading && messageList.length === 0">
          <el-empty description="暂无消息" />
        </div>

        <!-- 加载状态 -->
        <div class="loading-state" v-if="loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </div>
    </div>

    <!-- 移动端布局 -->
    <div class="mobile-layout" v-else>
      <!-- 顶部导航 -->
      <div class="mobile-header">
        <div class="mobile-header-left">
          <el-icon class="back-icon" @click="goBack"><ArrowLeft /></el-icon>
        </div>
        <div class="mobile-header-title">我的消息</div>
        <div class="mobile-header-right">
          <span class="read-all-btn" @click="markAllAsRead">全部已读</span>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="mobile-content">
        <div class="mobile-message-list">
          <div class="mobile-message-item"
               v-for="item in messageList"
               :key="item.id"
               :class="{ unread: !item.isRead }"
               @click="handleMessageClick(item)"
          >
            <div class="mobile-message-icon">
              <el-icon :size="20">
                <ChatDotRound v-if="item.type === 'system'" />
                <Bell v-else-if="item.type === 'notice'" />
                <Promotion v-else-if="item.type === 'promotion'" />
                <PriceTag v-else-if="item.type === 'price_drop'" />
                <Goods v-else-if="item.type === 'stock_alert'" />
                <ChatDotRound v-else />
              </el-icon>
              <div class="unread-dot" v-if="!item.isRead"></div>
            </div>
            <div class="mobile-message-info">
              <div class="mobile-message-top">
                <span class="mobile-message-title">{{ item.title }}</span>
                <span class="mobile-message-time">{{ item.time }}</span>
              </div>
              <p class="mobile-message-content">{{ item.content }}</p>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="mobile-empty" v-if="!loading && messageList.length === 0">
          <el-empty description="暂无消息" />
        </div>

        <!-- 加载状态 -->
        <div class="mobile-loading" v-if="loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDevice } from '@/hooks/useDevice'
import { ArrowLeft, Loading, ChatDotRound, Bell, Promotion, PriceTag, Goods } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMessageList, markAsRead, markAllAsRead as markAllAsReadAPI } from '@/api/message'

const router = useRouter()
const { isPc } = useDevice()

// 消息列表
const messageList = ref<any[]>([])
const loading = ref(false)

// 加载消息数据
const loadMessages = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const res = await getMessageList({ page: 1, pageSize: 50 })
    
    if (res.data && Array.isArray(res.data)) {
      messageList.value = res.data.map((item: any) => ({
        id: item.id,
        type: item.type || 'system', // system, notice, promotion, price_drop, stock_alert
        title: item.title || '',
        content: item.content || '',
        time: formatTime(item.createTime),
        isRead: item.isRead === 1 || item.isRead === true,
        // 购物车相关字段
        skuId: item.skuId,
        skuName: item.skuName,
        originalPrice: item.originalPrice,
        currentPrice: item.currentPrice,
        stockStatus: item.stockStatus
      }))
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 1000 / 60)
  const hours = Math.floor(diff / 1000 / 60 / 60)
  const days = Math.floor(diff / 1000 / 60 / 60 / 24)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取消息类型标签
const getTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    system: '系统通知',
    notice: '订单通知',
    promotion: '促销活动',
    price_drop: '降价提醒',
    stock_alert: '到货提醒'
  }
  return labelMap[type] || '其他通知'
}

// 处理消息点击
const handleMessageClick = async (item: any) => {
  // 标记为已读
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = true
      // 触发未读数更新事件
      window.dispatchEvent(new Event('unreadCountUpdate'))
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 根据消息类型跳转
  if (item.type === 'notice') {
    // 订单通知跳转到订单详情
    if (item.orderId) {
      router.push(`/order/detail?id=${item.orderId}`)
    }
  } else if (item.type === 'promotion') {
    // 促销活动跳转到活动页或首页
    router.push('/home')
  } else if (item.type === 'price_drop' || item.type === 'stock_alert') {
    // 降价提醒或到货提醒跳转到商品详情
    if (item.skuId) {
      router.push(`/detail/${item.skuId}`)
    }
  }
}

// 全部标为已读
const markAllAsRead = async () => {
  try {
    await markAllAsReadAPI()
    messageList.value.forEach(item => {
      item.isRead = true
    })
    ElMessage.success('已全部标为已读')
    // 触发未读数更新事件
    window.dispatchEvent(new Event('unreadCountUpdate'))
  } catch (error) {
    console.error('全部已读失败:', error)
    ElMessage.error('操作失败')
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面显示时刷新（用于移动端返回时刷新数据）
const handlePageShow = () => {
  loadMessages()
}

onMounted(() => {
  loadMessages()
  // 监听页面显示事件
  window.addEventListener('pageshow', handlePageShow)
})

onUnmounted(() => {
  window.removeEventListener('pageshow', handlePageShow)
})
</script>

<style scoped>
/* PC端样式 */
.pc-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.header-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-right {
  flex: 1;
  text-align: right;
}

.message-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.message-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.message-item.unread {
  background: #f0f7ff;
  border-color: #409eff;
}

.message-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 50%;
  margin-right: 16px;
  flex-shrink: 0;
}

.message-item.unread .message-icon {
  background: #e6f7ff;
  color: #409eff;
}

.message-info {
  flex: 1;
}

.message-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.message-type {
  font-size: 12px;
  color: #409eff;
  background: #e6f7ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.message-content-text {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.loading-icon {
  font-size: 24px;
  margin-right: 8px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 移动端样式 */
.mobile-layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 100;
}

.mobile-header-left {
  width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 20px;
  color: #333;
}

.mobile-header-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.mobile-header-right {
  width: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.read-all-btn {
  font-size: 14px;
  color: #409eff;
  cursor: pointer;
}

.mobile-content {
  padding: 10px;
}

.mobile-message-list {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.mobile-message-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  position: relative;
}

.mobile-message-item:last-child {
  border-bottom: none;
}

.mobile-message-item:active {
  background: #f5f5f5;
}

.mobile-message-item.unread {
  background: #f0f7ff;
}

.mobile-message-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 50%;
  margin-right: 12px;
  flex-shrink: 0;
  position: relative;
}

.mobile-message-item.unread .mobile-message-icon {
  background: #e6f7ff;
  color: #409eff;
}

.unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 2px solid #fff;
}

.mobile-message-info {
  flex: 1;
  min-width: 0;
}

.mobile-message-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.mobile-message-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mobile-message-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  margin-left: 10px;
}

.mobile-message-content {
  font-size: 13px;
  color: #666;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.mobile-empty {
  padding: 100px 20px;
  background: #fff;
  border-radius: 8px;
  text-align: center;
}

.mobile-loading {
  text-align: center;
  padding: 40px;
  color: #666;
  background: #fff;
  border-radius: 8px;
}
</style>

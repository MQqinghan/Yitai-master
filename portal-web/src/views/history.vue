<template>
  <div class="history-page">
    <!-- PC端布局 -->
    <div class="pc-layout" v-if="isPc">
      <!-- 顶部导航 -->
      <div class="history-header">
        <div class="header-left">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回</span>
          </el-button>
        </div>
        <h2 class="header-title">历史浏览</h2>
        <div class="header-right">
          <el-button type="danger" size="small" @click="clearHistory">清空记录</el-button>
        </div>
      </div>

      <!-- 历史列表 -->
      <div class="history-content">
        <div class="history-list">
          <div class="history-item"
               v-for="item in historyList"
               :key="item.id"
               @click="goToDetail(item.id)"
          >
            <img :src="item.imgUrl" :alt="item.name" class="item-img" />
            <div class="item-info">
              <p class="item-name">{{ item.name }}</p>
              <p class="item-price">¥{{ item.price }}</p>
              <p class="item-time">{{ item.browseTime }}</p>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-if="!loading && historyList.length === 0">
          <el-empty description="暂无浏览记录" />
          <el-button type="primary" @click="goHome">去逛逛</el-button>
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
        <div class="mobile-header-title">历史浏览</div>
        <div class="mobile-header-right">
          <span class="clear-btn" @click="clearHistory">清空</span>
        </div>
      </div>

      <!-- 历史列表 -->
      <div class="mobile-content">
        <div class="mobile-history-list">
          <div class="mobile-history-item"
               v-for="item in historyList"
               :key="item.id"
               @click="goToDetail(item.id)"
          >
            <img :src="item.imgUrl" :alt="item.name" class="mobile-item-img" />
            <div class="mobile-item-info">
              <p class="mobile-item-name">{{ item.name }}</p>
              <p class="mobile-item-price">¥{{ item.price }}</p>
              <p class="mobile-item-time">{{ item.browseTime }}</p>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="mobile-empty" v-if="!loading && historyList.length === 0">
          <el-empty description="暂无浏览记录" />
          <el-button type="primary" @click="goHome">去逛逛</el-button>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDevice } from '@/hooks/useDevice'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBrowseHistory } from '@/api/product'

const router = useRouter()
const { isPc } = useDevice()

// 历史浏览列表
const historyList = ref<any[]>([])
const loading = ref(false)

// 加载历史浏览数据
const loadHistory = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    // 获取最近浏览的100条记录
    const res = await getBrowseHistory(100)
    if (res.data && Array.isArray(res.data)) {
      // 过滤一周内的记录（一周 = 7天 = 7 * 24 * 60 * 60 * 1000 毫秒）
      const oneWeekAgo = Date.now() - 7 * 24 * 60 * 60 * 1000
      
      historyList.value = res.data
        .filter((item: any) => {
          const itemTime = new Date(item.updateTime || item.createTime).getTime()
          return itemTime >= oneWeekAgo
        })
        .map((item: any) => ({
          id: item.id,
          name: item.skuName || item.name,
          price: item.salePrice || item.price,
          imgUrl: item.thumbImg || item.image,
          browseTime: formatTime(item.updateTime || item.createTime)
        }))
      
      console.log('历史浏览数据（一周内）:', historyList.value.length, '条')
    }
  } catch (error) {
    console.error('加载历史浏览失败:', error)
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
  
  // 超过一周显示具体日期
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 跳转到商品详情
const goToDetail = (id: number) => {
  router.push(`/detail/${id}`)
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 返回首页
const goHome = () => {
  router.push('/home')
}

// 清空历史记录
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有浏览记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用后端清空接口
    // await clearBrowseHistory()
    
    historyList.value = []
    ElMessage.success('已清空浏览记录')
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
/* PC端样式 */
.pc-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.history-header {
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

.history-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.history-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.history-item {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.history-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.item-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.item-info {
  padding: 12px;
}

.item-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: nowrap;
  height: 20px;
}

.item-price {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.item-time {
  font-size: 12px;
  color: #999;
  margin: 0;
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
  width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-btn {
  font-size: 14px;
  color: #ff4d4f;
  cursor: pointer;
}

.mobile-content {
  padding: 10px;
}

.mobile-history-list {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.mobile-history-item {
  display: flex;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.mobile-history-item:last-child {
  border-bottom: none;
}

.mobile-history-item:active {
  background: #f5f5f5;
}

.mobile-item-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 12px;
  flex-shrink: 0;
}

.mobile-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.mobile-item-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: nowrap;
  height: 20px;
}

.mobile-item-price {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.mobile-item-time {
  font-size: 12px;
  color: #999;
  margin: 0;
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

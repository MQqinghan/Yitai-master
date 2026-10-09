<template>
  <div class="my-page">
    <!-- 移动端布局 -->
    <div class="mobile-my" v-if="!isPc">
      <!-- 顶部个人信息栏（参考示例图） -->
      <div class="mobile-header">
        <div class="header-bg"></div>
        <!-- 右上角消息+设置图标 -->
        <div class="header-actions">
          <div class="message-icon-wrapper" @click="goMessage">
            <el-icon class="action-icon message-icon">
              <ChatDotRound />
            </el-icon>
            <span class="unread-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </div>
          <el-icon class="action-icon setting-icon" @click="goSetting">
            <Setting />
          </el-icon>
        </div>
        <div class="user-info">
          <div class="avatar-box">
            <img :src="userInfo.avatar || defaultAvatar" alt="头像" class="avatar" />
          </div>
          <div class="user-name">
            {{ userInfo.nickName || '游客' }}
          </div>
        </div>
        <div class="member-bar">
          <span class="member-tag">DCloud会员</span>
          <span class="open-btn" @click="handleOpenMember">立即开通</span>
        </div>
        <div class="stats-row">
          <div class="stats-item">
            <p class="stats-value">{{ userInfo.balance || '128.8' }}</p>
            <p class="stats-label">余额</p>
          </div>
          <div class="stats-item">
            <p class="stats-value">{{ userInfo.coupon || '0' }}</p>
            <p class="stats-label">优惠券</p>
          </div>
          <div class="stats-item">
            <p class="stats-value">{{ userInfo.points || '20' }}</p>
            <p class="stats-label">积分</p>
          </div>
        </div>
      </div>

      <!-- 订单快捷入口 -->
      <div class="order-nav">
        <div class="nav-item" @click="goOrder('all')">
          <el-icon class="nav-icon"><Document /></el-icon>
          <span>全部订单</span>
        </div>
        <div class="nav-item" @click="goOrder('waitPay')">
          <el-icon class="nav-icon"><Money /></el-icon>
          <span>待付款</span>
        </div>
        <div class="nav-item" @click="goOrder('waitReceive')">
          <el-icon class="nav-icon"><Box /></el-icon>
          <span>待收货</span>
        </div>
        <div class="nav-item" @click="goOrder('canceled')">
          <el-icon class="nav-icon"><RefreshRight /></el-icon>
          <span>已取消</span>
        </div>
      </div>

      <!-- 功能入口栏（收藏、足迹等） -->
      <div class="func-nav">
        <div class="nav-item" @click="goFavorite">
          <el-icon class="nav-icon"><Star /></el-icon>
          <span>收藏</span>
        </div>
        <div class="nav-item" @click="goHistory">
          <el-icon class="nav-icon"><Timer /></el-icon>
          <span>我的足迹</span>
        </div>
        <div class="nav-item" @click="goFollowShop">
          <el-icon class="nav-icon"><Shop /></el-icon>
          <span>关注店铺</span>
        </div>
        <div class="nav-item" @click="goExpress">
          <el-icon class="nav-icon"><Van /></el-icon>
          <span>快递</span>
        </div>
      </div>

      <!-- 猜你喜欢 -->
      <div class="guess-like-section">
        <h3 class="section-title">猜你喜欢</h3>
        <div class="like-list">
          <div class="like-card"
               v-for="(item, index) in guessLikeList"
               :key="index"
               @click="handleProductClick(item.id)"
          >
            <img :src="item.imgUrl" alt="猜你喜欢" class="like-img" />
            <p class="like-name">{{ item.name }}</p>
            <span class="like-price">¥{{ item.price }}</span>
          </div>
        </div>
        
        <!-- 加载更多提示 -->
        <div class="loading-more" v-if="loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        
        <!-- 加载完成提示 -->
        <div class="load-complete" v-if="!loading && guessLikeList.length > 0 && !hasMore">
          <span>— 已经到底了 —</span>
        </div>
      </div>
    </div>

    <!-- PC端布局（左右结构） -->
    <div class="pc-my" v-else>
      <div class="pc-container">
        <!-- 左侧菜单栏 -->
        <div class="pc-left-menu">
          <div class="menu-item" @click="goCart">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车</span>
          </div>
          <div class="menu-item" @click="goOrder('all')">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </div>
          <div class="menu-item" @click="goCoupon">
            <el-icon><Ticket /></el-icon>
            <span>我的卡券</span>
          </div>
          <div class="menu-item" @click="goFollowShop">
            <el-icon><Shop /></el-icon>
            <span>关注店铺</span>
          </div>
          <div class="menu-item" @click="goFavorite">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </div>
          <div class="menu-item" @click="goMessage">
            <el-icon><ChatDotRound /></el-icon>
            <span>我的消息</span>
            <span class="menu-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </div>
          <div class="menu-item" @click="goSetting">
            <el-icon><Setting /></el-icon>
            <span>账户设置</span>
          </div>
        </div>

        <!-- 右侧内容区 -->
        <div class="pc-right-content">
          <!-- 第一栏：个人信息 -->
          <div class="info-card">
            <div class="info-left">
              <img :src="userInfo.avatar || defaultAvatar" alt="头像" class="avatar" />
              <div class="info-text">
                <p class="user-name">{{ userInfo.nickName || '游客' }}</p>
                <p class="user-desc">点击编辑个人资料</p>
              </div>
            </div>
            <div class="info-right">
              <div class="stats-item">
                <p class="stats-value">{{ userInfo.balance || '128.8' }}</p>
                <p class="stats-label">余额</p>
              </div>
              <div class="stats-item">
                <p class="stats-value">{{ userInfo.coupon || '0' }}</p>
                <p class="stats-label">优惠券</p>
              </div>
              <div class="stats-item">
                <p class="stats-value">{{ userInfo.points || '20' }}</p>
                <p class="stats-label">积分</p>
              </div>
            </div>
          </div>

          <!-- 第二栏：我的订单 + 快捷入口 -->
          <div class="order-section">
            <div class="order-left">
              <div class="section-header">
                <h3>我的订单</h3>
                <span class="more-link" @click="goOrder('all')">查看全部</span>
              </div>
              <div class="order-tabs">
                <div class="tab-item" @click="goOrder('waitPay')">
                  待付款
                  <span class="badge" v-if="orderCounts.waitPay > 0">{{ orderCounts.waitPay }}</span>
                </div>
                <div class="tab-item" @click="goOrder('waitSend')">
                  待发货
                  <span class="badge" v-if="orderCounts.waitSend > 0">{{ orderCounts.waitSend }}</span>
                </div>
                <div class="tab-item" @click="goOrder('waitReceive')">
                  待收货
                  <span class="badge" v-if="orderCounts.waitReceive > 0">{{ orderCounts.waitReceive }}</span>
                </div>
                <div class="tab-item" @click="goOrder('waitComment')">
                  待评价
                  <span class="badge" v-if="orderCounts.waitComment > 0">{{ orderCounts.waitComment }}</span>
                </div>
                <div class="tab-item" @click="goOrder('afterSale')">
                  售后
                  <span class="badge" v-if="orderCounts.afterSale > 0">{{ orderCounts.afterSale }}</span>
                </div>
              </div>
            </div>
            <div class="order-right">
              <div class="quick-entry" @click="goCollection">
                <el-icon><Star /></el-icon>
                <span>我的收藏</span>
              </div>
              <div class="quick-entry" @click="goCart">
                <el-icon><ShoppingCart /></el-icon>
                <span>购物车</span>
              </div>
            </div>
          </div>

          <!-- 第三栏：历史浏览 -->
          <div class="history-section">
            <div class="section-header">
              <h3>历史浏览</h3>
              <span class="more-link" @click="goHistory">查看全部</span>
            </div>
            <div class="history-list">
              <div class="history-item"
                   v-for="(item, index) in historyList"
                   :key="index"
                   @click="handleProductClick(item.id)"
              >
                <img :src="item.imgUrl" alt="历史商品" class="history-img" />
                <p class="history-name">{{ item.name }}</p>
                <span class="history-price">¥{{ item.price }}</span>
              </div>
            </div>
          </div>

          <!-- 第四栏：猜你喜欢 -->
          <div class="guess-like-section">
            <h3 class="section-title">猜你喜欢</h3>
            <div class="like-list">
              <div class="like-card"
                   v-for="(item, index) in guessLikeList"
                   :key="index"
                   @click="handleProductClick(item.id)"
              >
                <img :src="item.imgUrl" alt="猜你喜欢" class="like-img" />
                <p class="like-name">{{ item.name }}</p>
                <span class="like-price">¥{{ item.price }}</span>
              </div>
            </div>
            
            <!-- 加载更多提示 -->
            <div class="loading-more" v-if="loading">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            
            <!-- 加载完成提示 -->
            <div class="load-complete" v-if="!loading && guessLikeList.length > 0 && !hasMore">
              <span>— 已经到底了 —</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDevice } from '@/hooks/useDevice'
import { loadUserInfo, userInfo } from '@/hooks/useUser'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import {
  Document, Money, Box, RefreshRight, Star, Timer, Shop, Van,
  ShoppingCart, Ticket, Setting, ChatDotRound
} from '@element-plus/icons-vue'
import { getOrderList } from '@/api/order'
import { getGuessLikeList, getBrowseHistory } from '@/api/product'
import { getUnreadCount } from '@/api/message'

const router = useRouter()
const { isPc } = useDevice()

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 订单数量统计
const orderCounts = ref({
  waitPay: 0,       // 待付款
  waitSend: 0,      // 待发货
  waitReceive: 0,   // 待收货
  waitComment: 0,   // 待评价
  afterSale: 0      // 售后
})

// 加载订单数量
const loadOrderCounts = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  try {
    const statusMap = {
      waitPay: 0,       // 待付款
      waitSend: 1,      // 待发货
      waitReceive: 2,   // 已发货(待收货)
      waitComment: 3,   // 已完成(待评价)
      afterSale: -1     // 已取消
    }
    
    const promises = Object.entries(statusMap).map(async ([key, status]) => {
      const res = await getOrderList(1, 1, status)
      orderCounts.value[key] = res.data?.total || 0
    })
    
    await Promise.all(promises)
  } catch (error) {
    console.error('加载订单数量失败:', error)
  }
}

// 加载用户信息
onMounted(() => {
  loadUserInfo()
  loadOrderCounts()
  loadGuessLike()
  loadHistory()
  loadUnreadCount()
  
  // 监听未读数更新事件
  window.addEventListener('unreadCountUpdate', loadUnreadCount)
  // 监听页面显示事件
  window.addEventListener('pageshow', handlePageShow)
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('unreadCountUpdate', loadUnreadCount)
  window.removeEventListener('pageshow', handlePageShow)
  window.removeEventListener('scroll', handleScroll)
})

// 页面显示时刷新
const handlePageShow = () => {
  loadUnreadCount()
}

// 其他数据保持不变...

// 猜你喜欢列表
const guessLikeList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(6)
const hasMore = ref(true)
const loading = ref(false)

// 加载猜你喜欢数据
const loadGuessLike = async (isLoadMore = false) => {
  if (isLoadMore && !hasMore.value) return
  
  const token = localStorage.getItem('token')
  if (!token && !isLoadMore) {
    // 未登录时使用默认数据
    guessLikeList.value = [
      { id: 1, name: '纯棉短袖T恤', price: 99.9, imgUrl: 'https://via.placeholder.com/200x200?text=T恤' },
      { id: 2, name: '休闲牛仔裤', price: 159, imgUrl: 'https://via.placeholder.com/200x200?text=牛仔裤' },
      { id: 3, name: '运动跑鞋', price: 299, imgUrl: 'https://via.placeholder.com/200x200?text=跑鞋' },
      { id: 4, name: '时尚双肩包', price: 129, imgUrl: 'https://via.placeholder.com/200x200?text=双肩包' },
      { id: 5, name: '无线蓝牙耳机', price: 199, imgUrl: 'https://via.placeholder.com/200x200?text=耳机' },
      { id: 6, name: '智能手表', price: 399, imgUrl: 'https://via.placeholder.com/200x200?text=手表' }
    ]
    hasMore.value = false
    return
  }
  
  if (!token) {
    return
  }
  
  loading.value = true
  try {
    const res = await getGuessLikeList(currentPage.value, pageSize.value)
    if (res.data) {
      const newList = res.data.map((item: any) => ({
        id: item.id,
        name: item.skuName || item.name,
        price: item.price || item.salePrice,
        imgUrl: item.thumbImg || item.image
      }))
      
      if (isLoadMore) {
        guessLikeList.value = [...guessLikeList.value, ...newList]
      } else {
        guessLikeList.value = newList
      }
      
      // 如果返回的数据少于pageSize，说明没有更多数据了
      hasMore.value = newList.length >= pageSize.value
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载猜你喜欢失败:', error)
    if (!isLoadMore) {
      ElMessage.error('加载猜你喜欢失败')
    }
  } finally {
    loading.value = false
  }
}

// 滚动事件处理
const handleScroll = () => {
  const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const clientHeight = document.documentElement.clientHeight || document.body.clientHeight
  
  if (scrollHeight - scrollTop - clientHeight < 200) {
    if (!loading.value && hasMore.value) {
      currentPage.value++
      loadGuessLike(true)
    }
  }
}

// 历史浏览列表
const historyList = ref<any[]>([])

// 未读消息数量
const unreadCount = ref(0)

// 加载未读消息数量
const loadUnreadCount = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (error) {
    console.error('加载未读消息数量失败:', error)
  }
}

// 加载历史浏览数据
const loadHistory = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  try {
    const res = await getBrowseHistory(5)
    
    if (res.data && Array.isArray(res.data)) {
      historyList.value = res.data.map((item: any) => ({
        id: item.id,
        name: item.skuName || item.name,
        price: item.salePrice || item.price,
        imgUrl: item.thumbImg || item.image
      }))
    }
  } catch (error) {
    console.error('加载历史浏览失败:', error)
  }
}

// 页面跳转方法
const handleProductClick = (id: number) => {
  router.push(`/detail/${id}`)
}

const goOrder = (type: string) => {
  // 添加登录校验
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  
  // 根据类型跳转到订单列表页，传递状态参数
  const statusMap: Record<string, string> = {
    'all': '',
    'waitPay': '0',
    'waitSend': '1',
    'waitReceive': '2',
    'waitComment': '3',
    'canceled': '-1',
    'afterSale': ''
  }
  
  const status = statusMap[type] || ''
  router.push({
    path: '/order/list',
    query: status ? { status } : {}
  })
}

const goCart = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/cart')
  } else {
    router.push('/login')
  }
}

const goFavorite = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/collection')
  } else {
    router.push('/login')
  }
}

const goCollection = () => {
  goFavorite()
}

const goHistory = () => {
  router.push('/history')
}

const goFollowShop = () => {
  router.push('/follow-shop')
}

const goExpress = () => {
  router.push('/express')
}

const goCoupon = () => {
  router.push('/coupon')
}

const goSetting = () => {
  router.push('/setting')
}

const goMessage = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/message')
  } else {
    router.push('/login')
  }
}

const handleOpenMember = () => {
  ElMessage.info('会员功能开发中')
}
</script>

<style scoped>
@import '@/assets/css/my.css';
</style>
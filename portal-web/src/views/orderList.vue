<template>
  <div class="order-list-page">
    <!-- 顶部导航栏 -->
    <div class="order-header">
      <el-icon class="back-icon" @click="goBack">
        <ArrowLeft />
      </el-icon>
      <h2 class="page-title">我的订单</h2>
    </div>

    <!-- 订单状态筛选 -->
    <div class="order-tabs">
      <div
          class="tab-item"
          :class="{ active: currentStatus === item.value }"
          v-for="item in statusOptions"
          :key="item.value"
          @click="switchStatus(item.value)"
      >
        {{ item.label }}
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <div
          class="order-card"
          v-for="order in orderList"
          :key="order.id"
          @click="viewOrderDetail(order.id)"
      >
        <!-- 订单头部 -->
        <div class="order-card-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <span class="order-status" :class="getStatusClass(order.orderStatus)">
            {{ getStatusText(order.orderStatus) }}
          </span>
        </div>

        <!-- 商品列表 -->
        <div class="order-goods-list">
          <div class="goods-item" v-for="item in order.orderItemList" :key="item.skuId">
            <img :src="item.thumbImg || getDefaultImage()" class="goods-img" alt="商品图片" @error="handleImageError" @load="handleImageLoad" />
            <div class="goods-info">
              <p class="goods-name">{{ item.skuName }}</p>
              <p class="goods-price">
                <span class="price">¥{{ item.skuPrice }}</span>
                <span class="num">×{{ item.skuNum }}</span>
              </p>
            </div>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-card-footer">
          <span class="total-amount">
            共{{ order.orderItemList.length }}件商品，合计：
            <span class="amount">¥{{ order.totalAmount }}</span>
          </span>
          <div class="order-actions">
            <el-button
                v-if="order.orderStatus === 0"
                size="small"
                type="primary"
                @click.stop="goToPay(order.id)"
            >
              去支付
            </el-button>
            <el-button
                v-if="order.orderStatus === 3"
                size="small"
                type="success"
                @click.stop="confirmReceive(order.orderNo)"
            >
              确认收货
            </el-button>
            <el-button size="small" @click.stop="viewOrderDetail(order.id)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="orderList.length === 0 && !loading">
        <el-icon class="empty-icon"><ShoppingCart /></el-icon>
        <p class="empty-text">暂无订单</p>
      </div>

      <!-- 加载中提示 -->
      <div class="loading" v-if="loading">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <!-- 没有更多数据 -->
      <div class="no-more" v-if="!hasMore && orderList.length > 0">
        <p>没有更多订单了</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ShoppingCart, Loading } from '@element-plus/icons-vue'
import { getOrderList, updateOrderStatus } from '@/api/order'

const router = useRouter()
const route = useRoute()

// 订单列表
const orderList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 10
const hasMore = ref(true)

// 订单状态筛选
const currentStatus = ref('')
const statusOptions = [
  { label: '全部', value: '' },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: -1 }
]

// 根据路由参数初始化状态
onMounted(() => {
  // 从路由参数中获取状态筛选
  const queryStatus = route.query.status
  if (queryStatus !== undefined) {
    currentStatus.value = queryStatus === '' ? '' : Number(queryStatus)
  }
  
  // 加载订单列表
  loadOrderList()
})

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '待发货',
    2: '已发货',
    3: '已完成',
    '-1': '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-waiting',
    2: 'status-shipped',
    3: 'status-completed',
    '-1': 'status-cancelled'
  }
  return classMap[status] || ''
}

// 加载订单列表
const loadOrderList = async (isLoadMore = false) => {
  if (loading.value) return
  
  loading.value = true
  try {
    const page = isLoadMore ? currentPage.value : 1
    const res = await getOrderList(page, pageSize, currentStatus.value)
    
    if (res.data) {
      const newList = res.data.list || []
      
      // 调试信息：打印订单商品数据
      if (newList.length > 0 && newList[0].orderItemList?.length > 0) {
        console.log('订单商品数据:', JSON.parse(JSON.stringify(newList[0].orderItemList[0])))
        console.log('thumbImg 字段值:', newList[0].orderItemList[0].thumbImg)
      }
      
      if (isLoadMore) {
        orderList.value = [...orderList.value, ...newList]
      } else {
        orderList.value = newList
      }
      
      // 判断是否还有更多数据
      hasMore.value = orderList.value.length < res.data.total
      currentPage.value = page
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取默认图片
const getDefaultImage = () => {
  return 'https://picsum.photos/80/80?random=' + Math.random()
}

// 图片加载成功
const handleImageLoad = (event) => {
  console.log('图片加载成功:', event.target.src)
}

// 图片加载失败
const handleImageError = (event) => {
  console.log('图片加载失败，当前 URL:', event.target.src)
  event.target.src = getDefaultImage()
}

// 切换订单状态
const switchStatus = (status) => {
  currentStatus.value = status
  currentPage.value = 1
  hasMore.value = true
  loadOrderList()
}

// 加载更多
const loadMore = () => {
  currentPage.value++
  loadOrderList(true)
}

// 查看订单详情
const viewOrderDetail = (orderId) => {
  router.push({
    path: '/order/detail',
    query: { orderId }
  })
}

// 跳转到支付页面
const goToPay = (orderId) => {
  router.push({
    path: '/pay',
    query: { orderId }
  })
}

// 确认收货
const confirmReceive = (orderNo) => {
  ElMessageBox.confirm('确认已收到商品？', '确认收货', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用确认收货接口
      await updateOrderStatus(orderNo)
      ElMessage.success('确认收货成功')
      loadOrderList()
    } catch (error) {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }).catch(() => {})
}

// 返回上一页
const goBack = () => {
  router.back()
}
</script>

<style scoped>
@import '@/assets/css/orderList.css';
</style>

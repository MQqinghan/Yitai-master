<template>
  <div class="pay-success-page">
    <div class="success-container">
      <!-- 成功图标 -->
      <div class="success-icon">
        <el-icon :size="80" color="#52c41a">
          <CircleCheckFilled />
        </el-icon>
      </div>
      
      <!-- 成功提示 -->
      <h2 class="success-title">支付成功</h2>
      <p class="success-amount">支付金额：<span class="amount">¥{{ orderInfo?.totalAmount || '0.00' }}</span></p>
      <p class="success-order">订单号：{{ orderInfo?.orderNo }}</p>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button size="large" @click="viewOrderDetail">
          查看订单详情
        </el-button>
        <el-button type="primary" size="large" @click="goHome">
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { getOrderInfo } from '@/api/order'

const router = useRouter()
const route = useRoute()

const orderInfo = ref(null)

// 加载订单信息
const loadOrderInfo = async () => {
  let orderId = route.query.orderId
  let orderNo = route.query.orderNo
  
  console.log('支付成功页面 - 路由参数:', { orderId, orderNo })
  
  // 如果路由参数中没有，尝试从localStorage获取
  if (!orderId) {
    orderId = localStorage.getItem('pendingOrderId')
    console.log('从localStorage获取到orderId:', orderId)
  }
  if (!orderNo) {
    orderNo = localStorage.getItem('pendingOrderNo')
    console.log('从localStorage获取到orderNo:', orderNo)
  }
  
  if (!orderId && !orderNo) {
    console.error('订单ID和订单号都不存在，无法加载订单信息')
    ElMessage.warning('订单信息不存在')
    return
  }
  
  try {
    // 优先使用 orderId，如果没有则使用 orderNo
    const queryId = orderId || orderNo
    console.log('开始加载订单信息，查询ID:', queryId)
    
    const res = await getOrderInfo(queryId)
    console.log('订单信息响应:', res)
    
    if (res.data) {
      orderInfo.value = res.data
      console.log('订单信息加载成功:', orderInfo.value)
      
      // 不在这里清除localStorage，让orderDetail页面也能使用
      // localStorage.removeItem('pendingOrderId')
      // localStorage.removeItem('pendingOrderNo')
    } else {
      console.error('订单数据为空')
      ElMessage.error('订单数据为空')
    }
  } catch (error) {
    console.error('加载订单信息失败:', error)
    ElMessage.error('加载订单信息失败: ' + (error.message || '未知错误'))
  }
}

// 查看订单详情
const viewOrderDetail = () => {
  router.replace({
    path: '/order/detail',
    query: { orderId: route.query.orderId }
  })
}

// 返回首页
const goHome = () => {
  router.replace('/')
}

onMounted(() => {
  loadOrderInfo()
})
</script>

<style scoped>
@import '@/assets/css/paySuccess.css';
</style>

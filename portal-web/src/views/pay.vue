<template>
  <div class="pay-page">
    <!-- 顶部导航栏 -->
    <div class="pay-header">
      <el-icon class="back-icon" @click="goBack">
        <ArrowLeft />
      </el-icon>
      <h2 class="page-title">支付</h2>
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 支付内容 -->
    <template v-if="orderInfo && !loading">
      <!-- 支付金额 -->
      <div class="amount-card">
        <p class="amount-label">支付金额</p>
        <div class="amount-value">
          <span class="currency">¥</span>
          <span class="price">{{ orderInfo.totalAmount }}</span>
        </div>
      </div>

      <!-- 支付方式列表 -->
      <div class="pay-methods">
        <div 
          class="pay-method-item" 
          v-for="method in payMethods" 
          :key="method.id"
          @click="selectPayMethod(method.id)"
        >
          <div class="method-left">
            <div class="method-icon" :style="{ background: method.color }">
              <el-icon :size="24">
                <component :is="method.icon" />
              </el-icon>
            </div>
            <div class="method-info">
              <p class="method-name">{{ method.name }}</p>
              <p class="method-desc" v-if="method.desc">{{ method.desc }}</p>
            </div>
          </div>
          <el-icon class="radio-icon" :class="{ active: selectedMethod === method.id }">
            <Check v-if="selectedMethod === method.id" />
          </el-icon>
        </div>
      </div>

      <!-- 底部确认支付按钮 -->
      <div class="pay-footer">
        <el-button 
          type="danger" 
          size="large" 
          class="confirm-btn"
          @click="confirmPay"
          :disabled="!selectedMethod"
        >
          确认支付
        </el-button>
      </div>
    </template>

    <!-- 空状态 -->
    <div class="empty-state" v-if="!orderInfo && !loading">
      <el-icon class="empty-icon"><Document /></el-icon>
      <p class="empty-text">订单信息不存在</p>
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading, Document, ChatDotRound, Wallet, Money, Check } from '@element-plus/icons-vue'
import { getOrderInfo, updateOrderStatus, submitAlipay, getPayStatus } from '@/api/order'

const router = useRouter()
const route = useRoute()

// 订单信息
const orderInfo = ref(null)
const loading = ref(false)

// 选中的支付方式
const selectedMethod = ref(2) // 默认选中支付宝

// 轮询定时器
let pollTimer = null

// 支付方式列表
const payMethods = [
  {
    id: 1,
    name: '微信支付',
    desc: '推荐使用微信支付',
    icon: ChatDotRound,
    color: '#09bb07'
  },
  {
    id: 2,
    name: '支付宝支付',
    desc: '',
    icon: Wallet,
    color: '#1677ff'
  },
  {
    id: 3,
    name: '预存款支付',
    desc: '可用余额 ¥198.5',
    icon: Money,
    color: '#ff6b6b'
  }
]

// 选择支付方式
const selectPayMethod = (methodId) => {
  selectedMethod.value = methodId
}

// 确认支付
const confirmPay = async () => {
  if (!selectedMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  if (!orderInfo.value) {
    ElMessage.error('订单信息不存在')
    return
  }

  try {
    if (selectedMethod.value === 2) {
      // === 支付宝支付 ===
      console.log('开始支付宝支付，订单信息:', orderInfo.value)
      console.log('订单号:', orderInfo.value.orderNo)
      
      // 保存订单信息到 localStorage，防止支付宝跳转回来后丢失
      localStorage.setItem('pendingOrderId', orderInfo.value.id)
      localStorage.setItem('pendingOrderNo', orderInfo.value.orderNo)
      
      ElMessage.info('正在跳转支付宝...')
      
      // 调用后端接口获取支付宝HTML表单
      const res = await submitAlipay(orderInfo.value.orderNo)
      
      console.log('支付宝接口返回:', res)
      
      if (res.data) {
        // 创建一个隐藏的div来存放支付宝表单
        const div = document.createElement('div')
        div.innerHTML = res.data
        document.body.appendChild(div)
        
        // 自动提交表单，跳转到支付宝沙箱页面
        const form = div.querySelector('form')
        if (form) {
          form.submit()
        }
        
        // 开始轮询查询支付状态
        startPollingPayStatus(orderInfo.value.orderNo)
      } else {
        ElMessage.error('获取支付宝支付信息失败')
      }
    } else {
      // === 其他支付方式（模拟） ===
      // 实际项目中这里调用对应的支付接口
      await updateOrderStatus(orderInfo.value.orderNo)
      
      // 跳转到支付成功页面
      router.replace({
        path: '/pay/success',
        query: { orderId: orderInfo.value.id }
      })
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请重试')
  }
}

// 加载订单信息
const loadOrderInfo = async () => {
  let orderId = route.query.orderId
  console.log('pay页面 - 路由参数orderId:', orderId)
  
  // 如果路由参数中没有orderId，尝试从localStorage获取
  if (!orderId) {
    orderId = localStorage.getItem('pendingOrderId')
    console.log('从localStorage获取到orderId:', orderId)
  }
  
  if (!orderId) {
    ElMessage.warning('订单ID不存在')
    return
  }

  loading.value = true
  try {
    console.log('开始加载订单信息，订单ID:', orderId)
    const res = await getOrderInfo(orderId)
    console.log('订单信息响应:', res)
    
    if (res.data) {
      orderInfo.value = res.data
      console.log('订单信息加载成功:', orderInfo.value)
    } else {
      console.error('订单数据为空')
    }
  } catch (error) {
    console.error('加载订单信息失败:', error)
    ElMessage.error('加载订单信息失败')
  } finally {
    loading.value = false
  }
}

// 开始轮询查询支付状态
const startPollingPayStatus = (orderNo) => {
  // 保存订单ID和订单号，防止页面刷新后丢失
  const currentOrderId = orderInfo.value?.id
  const currentOrderNo = orderInfo.value?.orderNo
  
  console.log('开始轮询支付状态，订单ID:', currentOrderId, '订单号:', currentOrderNo)
  
  // 每3秒查询一次
  pollTimer = setInterval(async () => {
    try {
      const res = await getPayStatus(orderNo)
      console.log('轮询支付状态返回:', res)
      
      if (res.data && res.data.orderStatus === 1) {
        // 支付成功，停止轮询
        clearInterval(pollTimer)
        pollTimer = null
        
        ElMessage.success('支付成功！')
        // 跳转到支付成功页面，同时传递 orderId 和 orderNo
        router.replace({
          path: '/pay/success',
          query: { 
            orderId: currentOrderId,
            orderNo: currentOrderNo 
          }
        })
      }
    } catch (error) {
      console.error('查询支付状态失败:', error)
    }
  }, 3000)
}

// 返回上一页
const goBack = () => {
  // 清除轮询定时器
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  router.back()
}

onMounted(() => {
  loadOrderInfo()
})

onUnmounted(() => {
  // 组件卸载时清除定时器
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>

<style scoped>
@import '@/assets/css/pay.css';
</style>

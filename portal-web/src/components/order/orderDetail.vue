<template>
  <div class="order-detail-page">
    <!-- 顶部导航栏 -->
    <div class="detail-header">
      <el-icon class="back-icon" @click="goBack">
        <ArrowLeft />
      </el-icon>
      <h2 class="page-title">订单详情</h2>
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 订单详情内容 -->
    <template v-if="orderInfo && !loading">
      <!-- 订单状态卡片 -->
      <div class="status-card">
        <div class="status-icon">
          <el-icon :size="40" :color="getStatusColor(orderInfo.orderStatus)">
            <component :is="getStatusIcon(orderInfo.orderStatus)" />
          </el-icon>
        </div>
        <h3 class="status-text">{{ getStatusText(orderInfo.orderStatus) }}</h3>
        <p class="order-time" v-if="orderInfo.paymentTime">
          支付时间：{{ formatDate(orderInfo.paymentTime) }}
        </p>
      </div>

      <!-- 收货地址 -->
      <div class="info-section">
        <h4 class="section-title">收货信息</h4>
        <div class="address-info">
          <div class="receiver-info">
            <span class="receiver-name">{{ orderInfo.receiverName }}</span>
            <span class="receiver-phone">{{ orderInfo.receiverPhone }}</span>
          </div>
          <p class="receiver-address">
            {{ orderInfo.receiverProvince }} {{ orderInfo.receiverCity }} 
            {{ orderInfo.receiverDistrict }} {{ orderInfo.receiverAddress }}
          </p>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="info-section">
        <h4 class="section-title">商品信息</h4>
        <div class="goods-list">
          <div class="goods-item" v-for="item in orderInfo.orderItemList" :key="item.skuId">
            <img :src="item.thumbImg" class="goods-img" alt="" />
            <div class="goods-info">
              <p class="goods-name">{{ item.skuName }}</p>
              <div class="goods-price-row">
                <span class="price">¥{{ item.skuPrice }}</span>
                <span class="num">×{{ item.skuNum }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="info-section">
        <h4 class="section-title">订单信息</h4>
        <div class="order-info-list">
          <div class="info-item">
            <span class="label">订单编号</span>
            <span class="value">{{ orderInfo.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">创建时间</span>
            <span class="value">{{ formatDate(orderInfo.createTime) }}</span>
          </div>
          <div class="info-item" v-if="orderInfo.paymentTime">
            <span class="label">支付时间</span>
            <span class="value">{{ formatDate(orderInfo.paymentTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">支付方式</span>
            <span class="value">{{ getPayTypeText(orderInfo.payType) }}</span>
          </div>
          <div class="info-item">
            <span class="label">运费</span>
            <span class="value">¥{{ orderInfo.feightFee || 0 }}</span>
          </div>
          <div class="info-item total">
            <span class="label">订单总额</span>
            <span class="value amount">¥{{ orderInfo.totalAmount }}</span>
          </div>
        </div>
      </div>

      <!-- 订单备注 -->
      <div class="info-section" v-if="orderInfo.remark">
        <h4 class="section-title">订单备注</h4>
        <p class="remark-text">{{ orderInfo.remark }}</p>
      </div>

      <!-- 底部操作按钮 -->
      <div class="detail-footer" v-if="orderInfo.orderStatus === 0">
        <el-button size="large" @click="goBack">返回</el-button>
        <el-button size="large" type="primary" @click="payOrder(orderInfo.orderNo)">
          立即支付
        </el-button>
      </div>
    </template>

    <!-- 空状态 -->
    <div class="empty-state" v-if="!orderInfo && !loading">
      <el-icon class="empty-icon"><Document /></el-icon>
      <p class="empty-text">订单不存在</p>
      <el-button type="primary" @click="goToOrderList">查看订单列表</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Loading, Document, Clock, CircleCheck, Van, CircleClose } from '@element-plus/icons-vue'
import { getOrderInfo, updateOrderStatus } from '@/api/order'

const router = useRouter()
const route = useRoute()

// 订单信息
const orderInfo = ref(null)
const loading = ref(false)

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

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    0: '#e6a23c',
    1: '#409eff',
    2: '#67c23a',
    3: '#909399',
    '-1': '#f56c6c'
  }
  return colorMap[status] || '#909399'
}

// 获取状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    0: Clock,
    1: CircleCheck,
    2: Van,
    3: CircleCheck,
    '-1': CircleClose
  }
  return iconMap[status] || Clock
}

// 获取支付方式文本
const getPayTypeText = (type) => {
  const typeMap = {
    1: '微信支付',
    2: '支付宝'
  }
  return typeMap[type] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 加载订单详情
const loadOrderDetail = async () => {
  let orderId = route.query.orderId
  console.log('订单详情页面 - 路由参数orderId:', orderId)
  
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
    console.log('开始加载订单详情，订单ID:', orderId)
    const res = await getOrderInfo(orderId)
    console.log('订单详情响应:', res)
    
    if (res.data) {
      orderInfo.value = res.data
      console.log('订单详情加载成功:', orderInfo.value)
    } else {
      console.error('订单数据为空')
      ElMessage.error('订单数据为空')
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 支付订单
const payOrder = (orderNo) => {
  ElMessageBox.confirm('确认支付该订单？', '支付确认', {
    confirmButtonText: '确认支付',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 模拟支付成功，更新订单状态
      await updateOrderStatus(orderNo)
      ElMessage.success('支付成功')
      // 重新加载订单详情
      loadOrderDetail()
    } catch (error) {
      console.error('支付失败:', error)
      ElMessage.error('支付失败')
    }
  }).catch(() => {})
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到订单列表
const goToOrderList = () => {
  router.push('/order/list')
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
@import '@/assets/css/orderDetail.css';
</style>

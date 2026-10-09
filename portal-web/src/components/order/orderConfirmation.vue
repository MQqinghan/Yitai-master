<template>
  <div class="order-confirmation-page">
    <!-- 顶部导航栏（双端适配） -->
    <div class="order-header">
      <el-icon class="back-icon" @click="goBack">
        <ArrowLeft />
      </el-icon>
      <h2 class="page-title">确认订单</h2>
    </div>

    <!-- 收货地址 -->
    <div class="address-selector">
      <h4 class="modal-title">收货地址</h4>
      <div class="address-display" @click="goToAddressPage()">
        <span class="addr-text">
          {{ selectedAddress ? getSelectedAddressText() : '请选择收货地址' }}
        </span>
        <el-icon class="arrow-icon"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="goods-info-modal">
      <h4 class="modal-title">商品信息 ({{ orderItemList.length }})</h4>
      <div class="goods-item" v-for="item in orderItemList" :key="item.skuId">
        <img :src="item.thumbImg" class="goods-img-modal" alt="" />
        <div class="goods-desc-modal">
          <div class="goods-top-row">
            <p class="goods-name-modal">{{ item.skuName }}</p>
          </div>
          <div class="goods-bottom-row">
            <div class="goods-price-info">
              <span class="price-label">单价</span>
              <span class="price-value">¥{{ item.skuPrice }}</span>
            </div>
            <!-- 数量选择器 -->
            <div class="goods-count-selector">
              <span class="count-label">数量</span>
              <div class="count-controls">
                <el-button
                    size="small"
                    :disabled="item.skuNum <= 1"
                    @click="decreaseNum(item)"
                >
                  -
                </el-button>
                <span class="count-value">{{ item.skuNum }}</span>
                <el-button
                    size="small"
                    @click="increaseNum(item)"
                >
                  +
                </el-button>
              </div>
            </div>
            <div class="goods-subtotal">
              <span class="subtotal-label">小计</span>
              <span class="subtotal-value">¥{{ (item.skuPrice * item.skuNum).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部总价 + 支付 -->
    <div class="order-footer">
      <div class="total-price">
        总计：<span>¥{{ totalPrice.toFixed(2) }}</span>
      </div>
      <el-button
          type="primary"
          size="large"
          class="pay-btn"
          :disabled="!selectedAddress || orderItemList.length === 0"
          @click="submitOrder"
      >
        提交订单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElIcon, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getAddressList } from '@/api/address'
import { getTrade, buyTrade, submitOrder as submitOrderApi } from '@/api/order'  // 导入订单API

const router = useRouter()
const route = useRoute()

// 订单商品列表（从后端获取）
const orderItemList = ref([])

// 地址列表（从后端获取）
const addressList = ref([])
const selectedAddress = ref('')

// 从后端加载地址列表
const loadAddressList = async () => {
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
    
    // 加载完成后，如果有选中的地址，确保其存在
    if (selectedAddress.value) {
      const addr = addressList.value.find(a => a.id === selectedAddress.value)
      if (!addr) {
        // 如果选中的地址不存在，重置选择
        selectedAddress.value = ''
      }
    } else {
      // 如果没有选中的地址，自动选择默认地址
      const defaultAddr = addressList.value.find(a => a.isDefault === 1)
      if (defaultAddr) {
        selectedAddress.value = defaultAddr.id
      }
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
  }
}

// 从后端加载订单商品数据
const loadOrderData = async () => {
  try {
    let res
    // 判断来源：购物车结算 还是 立即购买
    if (route.query.skuId) {
      // 立即购买：单个商品
      res = await buyTrade(route.query.skuId)
    } else {
      // 购物车结算：获取选中商品
      res = await getTrade()
    }
    
    if (res.data) {
      orderItemList.value = res.data.orderItemList || []
    }
  } catch (error) {
    console.error('加载订单数据失败:', error)
    ElMessage.error('加载订单数据失败')
  }
}

// 增加商品数量
const increaseNum = (item) => {
  item.skuNum += 1
}

// 减少商品数量
const decreaseNum = (item) => {
  if (item.skuNum > 1) {
    item.skuNum -= 1
  }
}

// 页面加载时，加载数据
onMounted(() => {
  // 从本地读取地址（地址页返回后自动填充）
  const addrId = localStorage.getItem('selectedAddressId')
  if (addrId) {
    selectedAddress.value = Number(addrId)
    localStorage.removeItem('selectedAddressId')
  }
  
  // 从后端加载地址列表和订单数据
  loadAddressList()
  loadOrderData()
  
  // 监听页面显示事件（从地址页返回时触发）
  const handlePageShow = async () => {
    const id = localStorage.getItem('selectedAddressId')
    if (id) {
      selectedAddress.value = Number(id)
      localStorage.removeItem('selectedAddressId')
      
      // 重新加载地址列表以确保数据最新
      await loadAddressList()
      
      ElMessage.success('已选择收货地址')
    }
  }
  window.addEventListener('pageshow', handlePageShow)
  
  // 组件卸载时移除监听
  onUnmounted(() => {
    window.removeEventListener('pageshow', handlePageShow)
  })
})

// 返回上一页（回到购物车/商品详情页）
const goBack = () => {
  router.back()
}

// 跳转到地址页
const goToAddressPage = () => {
  router.push('/address?from=buy')
}

// 选择地址（PC端弹窗用，保留兼容）
const selectAddressFromModal = (addr) => {
  selectedAddress.value = addr.id
  ElMessage.success('已选择收货地址')
}

// 获取地址显示文本
const getSelectedAddressText = () => {
  if (!selectedAddress.value) return '请选择收货地址'
  
  const addr = addressList.value.find(a => a.id === selectedAddress.value)
  if (!addr) return '请选择收货地址'
  
  return `${addr.name} ${addr.phone} | ${addr.fullAddress || ''}`
}

// 总价计算
const totalPrice = computed(() => {
  return orderItemList.value.reduce((sum, item) => sum + item.skuPrice * item.skuNum, 0)
})

// 提交订单
const submitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (orderItemList.value.length === 0) {
    ElMessage.warning('没有可提交的商品')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认提交订单，总计 ¥${totalPrice.value.toFixed(2)}？`,
      '订单确认',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 组装订单数据
    const orderData = {
      userAddressId: selectedAddress.value,
      orderItemList: orderItemList.value,
      feightFee: 0  // 运费，根据实际业务设置
    }
    
    // 调用后端提交订单接口
    const res = await submitOrderApi(orderData)
    
    if (res.code === 200) {
      // 后端返回的是 OrderInfo 对象，res.data 就是订单对象
      const orderData = res.data
      const orderId = orderData.id
      
      console.log('订单提交成功，订单数据:', orderData)
      console.log('订单ID:', orderId)
      console.log('订单号:', orderData.orderNo)
      
      ElMessage.success('订单提交成功!')
      
      // 跳转到支付页面（传递 orderId）
      router.push({
        path: '/pay',
        query: { orderId }
      })
    } else {
      ElMessage.error(res.message || '订单提交失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== false) {
      console.error('提交订单失败:', error)
      ElMessage.error('提交订单失败')
    }
  }
}
</script>

<style scoped>
@import "@/assets/css/orderConfirmation.css";
</style>
<template>
  <div class="collection-page">
    <!-- 移动端 -->
    <div class="mobile-collection" v-if="!isPc">
      <!-- 顶部导航栏 -->
      <div class="mobile-header">
        <el-icon class="back-icon" @click="handleBack">
          <ArrowLeft />
        </el-icon>
        <div class="header-title">我的收藏（{{ total }}）</div>
        <div class="header-actions">
          <el-icon class="action-icon"><Search /></el-icon>
          <el-icon class="action-icon"><Setting /></el-icon>
        </div>
      </div>

      <!-- 二级分类栏 -->
      <div class="category-bar">
        <div class="category-list">
          <div class="category-item active">全部商品</div>
          <div class="category-item">新品</div>
          <div class="category-item">优惠</div>
        </div>
        <div class="filter-btn">
          <el-icon><Filter /></el-icon>
          <span>筛选</span>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="goods-list">
        <div class="goods-item" v-for="item in goodsList" :key="item.id">
          <div class="goods-img">
            <img :src="item.image" alt="" />
          </div>
          <div class="goods-info">
            <div class="goods-name">{{ item.name }}</div>
            <div class="goods-price">¥{{ item.price }}</div>
            <div class="shop-name">{{ item.shopName }}</div>
            <div class="goods-btns">
              <div class="mini-btn" @click="handleCancelCollect(item.skuId)">取消收藏</div>
              <div class="mini-btn">找相似</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- PC 端 -->
    <div class="pc-collection" v-else>
      <div class="pc-container">
        <!-- 左侧菜单 -->
        <div class="pc-left-menu">
          <div class="menu-item" @click="goCart">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车</span>
          </div>
          <div class="menu-item" @click="goOrder">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </div>
          <div class="menu-item">
            <el-icon><Ticket /></el-icon>
            <span>我的卡券</span>
          </div>
          <div class="menu-item">
            <el-icon><Shop /></el-icon>
            <span>关注店铺</span>
          </div>
          <div class="menu-item active">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </div>
          <div class="menu-item" @click="goSetting">
            <el-icon><Setting /></el-icon>
            <span>账户设置</span>
          </div>
        </div>

        <!-- 右侧内容 -->
        <div class="pc-right-content">
          <div class="panel">
            <div class="panel-header">
              <h2>我的收藏（{{ total }}）</h2>
              <div class="panel-actions">
                <el-button type="primary" link>管理</el-button>
                <el-button type="primary" link>筛选</el-button>
              </div>
            </div>

            <div class="pc-goods-list">
              <div class="pc-goods-item" v-for="item in goodsList" :key="item.id">
                <div class="pc-goods-img">
                  <img :src="item.image" alt="" />
                </div>
                <div class="pc-goods-info">
                  <div class="pc-goods-name">{{ item.name }}</div>
                  <div class="pc-goods-price">¥{{ item.price }}</div>
                  <div class="pc-shop-name">{{ item.shopName }}</div>
                  <div class="pc-goods-btns">
                    <el-button type="danger" size="small" @click="handleCancelCollect(item.skuId)">取消收藏</el-button>
                    <el-button type="default" size="small">找相似</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useDevice } from '@/hooks/useDevice'
import {
  ArrowLeft,
  Search,
  Setting,
  Filter,
  ShoppingCart,
  Document,
  Ticket,
  Shop,
  Star
} from '@element-plus/icons-vue'
import { getCollectList, cancelCollect } from '@/api/collect'  // 导入收藏 API

const router = useRouter()
const { isPc } = useDevice()

// 收藏总数
const total = ref(0)

// 收藏商品列表
const goodsList = ref([])

// 加载收藏列表
const loadCollectList = async () => {
  try {
    const list = await getCollectList()
    goodsList.value = list.map(item => ({
      id: item.id,
      skuId: item.skuId,
      name: item.skuName,
      price: item.salePrice,
      marketPrice: item.marketPrice,
      stockNum: item.stockNum,
      shopName: '店铺名称',  // TODO: 后续从后端获取
      image: item.thumbImg
    }))
    total.value = list.length
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error('加载收藏列表失败')
  }
}

// 取消收藏
const handleCancelCollect = async (skuId) => {
  try {
    await cancelCollect(skuId)
    ElMessage.success('已取消收藏')
    // 重新加载列表
    await loadCollectList()
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 页面加载时获取收藏列表
onMounted(() => {
  loadCollectList()
})

// 返回
const handleBack = () => {
  router.back()
}

// 去购物车
const goCart = () => {
  router.push('/cart')
}

// 去设置
const goSetting = () => {
  router.push('/setting')
}

// 去订单列表
const goOrder = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/order/list')
  } else {
    router.push('/login')
  }
}
</script>

<style scoped>
@import '@/assets/css/collection.css';
</style>
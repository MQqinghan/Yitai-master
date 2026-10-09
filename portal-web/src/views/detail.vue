<template>
  <div class="product-detail">
    <div class="detail-container pc-layout" v-show="!isMobile">
      <div class="detail-left">
        <el-carousel height="400px" arrow="hover">
          <el-carousel-item v-for="(img, index) in sliderUrlList" :key="index">
            <img :src="img" class="carousel-img" alt="" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <div class="detail-right">
        <h2 class="product-name">{{ product.name }}</h2>
        <p class="sku-name">{{ productSku.skuName }}</p>
        <div class="price-box">
          <span class="market-price">市场价：¥{{ productSku.marketPrice }}</span>
          <span class="sale-price">售价：¥{{ productSku.salePrice }}</span>
        </div>
        <div class="stock">库存：{{ productSku.stockNum }} 件</div>

        <div class="spec-group" v-for="spec in specValueList" :key="spec.key">
          <label>{{ spec.key }}：</label>
          <div class="spec-list">
            <div
                v-for="val in spec.valueList"
                :key="val"
                class="spec-btn"
                :class="{ active: selectedSpec[spec.key] === val }"
                @click="selectSpec(spec.key, val)"
            >
              {{ val }}
            </div>
          </div>
        </div>

        <div class="action-box">
          <div
              class="collect-btn"
              @click="toggleCollectHandler"
              :class="{ collected: isCollected }"
          >
            <el-icon>
              <Star v-if="!isCollected" />
              <StarFilled v-else />
            </el-icon>
          </div>
          <el-button type="primary" size="large" class="add-cart" @click="addCart">
            加入购物车
          </el-button>
          <el-button type="danger" size="large" class="buy-now" @click="openBuyModal">
            立即购买
          </el-button>
        </div>
      </div>
    </div>

    <div class="mobile-layout" v-show="isMobile">
      <el-carousel height="250px" arrow="never">
        <div class="float-back" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <el-carousel-item v-for="(img, i) in sliderUrlList" :key="i">
          <img :src="img" class="carousel-img" alt="" />
        </el-carousel-item>
      </el-carousel>

      <div class="mobile-info">
        <h3>{{ product.name }}</h3>
        <p class="sku-name">{{ productSku.skuName }}</p>
        <div class="price">
          <span class="sale">¥{{ productSku.salePrice }}</span>
          <span class="market">¥{{ productSku.marketPrice }}</span>
        </div>
        <div class="stock">库存：{{ productSku.stockNum }}</div>
      </div>

      <div class="spec-group mobile-spec" v-for="spec in specValueList" :key="spec.key">
        <label>{{ spec.key }}</label>
        <div class="spec-list">
          <div
              v-for="val in spec.valueList"
              :key="val"
              class="spec-btn"
              :class="{ active: selectedSpec[spec.key] === val }"
              @click="selectSpec(spec.key, val)"
          >
            {{ val }}
          </div>
        </div>
      </div>

      <div class="mobile-bottom">
        <div
            class="collect-btn mobile-collect"
            @click="toggleCollectHandler"
            :class="{ collected: isCollected }"
        >
          <el-icon>
            <Star v-if="!isCollected" />
            <StarFilled v-else />
          </el-icon>
        </div>
        <el-button type="primary" @click="addCart">加入购物车</el-button>
        <el-button type="danger" @click="openBuyModal">立即购买</el-button>
      </div>
    </div>

    <div class="detail-images">
      <h3>商品详情</h3>
      <img v-for="(img, idx) in detailsImageUrlList" :key="idx" :src="img" class="detail-img" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElIcon, ElMessageBox } from 'element-plus'
import { ArrowLeft, Star, StarFilled } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { addToCart } from '@/api/cart'
import { collect, cancelCollect, isCollected as checkIsCollected } from '@/api/collect'  // 导入收藏 API
import { getProductDetail, recordBrowse } from '@/api/product'  // 导入商品详情和浏览记录 API

const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}
window.addEventListener('resize', checkMobile)

const router = useRouter()
const route = useRoute()
const skuId = route.params.skuId

const goBack = () => {
  router.back()
}

const isCollected = ref(false)

// 切换收藏状态
const toggleCollectHandler = async () => {
  try {
    if (isCollected.value) {
      // 已收藏，取消收藏
      await cancelCollect(skuId)
      isCollected.value = false
      ElMessage.success('已取消收藏')
    } else {
      // 未收藏，添加收藏
      await collect(skuId)
      isCollected.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 商品详情数据
const productSku = ref({
  id: null,
  skuName: '',
  thumbImg: '',
  salePrice: 0,
  marketPrice: 0,
  stockNum: 0
})

const product = ref({
  name: ''
})

const sliderUrlList = ref([])
const detailsImageUrlList = ref([])
const specValueList = ref([])

const selectedSpec = reactive({})

const buyNum = ref(1)

// 加载商品详情
const loadProductDetail = async () => {
  if (!skuId) {
    ElMessage.error('商品ID不存在')
    return
  }
  
  try {
    const res = await getProductDetail(skuId)
    
    if (res.data) {
      const data = res.data
      
      // 设置商品信息（从 product 对象中获取）
      if (data.product) {
        product.value.name = data.product.name || ''
      }
      
      // 设置SKU信息（从 productSku 对象中获取）
      if (data.productSku) {
        productSku.value = {
          id: data.productSku.id,
          skuName: data.productSku.skuName || '',
          thumbImg: data.productSku.thumbImg || '',
          salePrice: data.productSku.salePrice || 0,
          marketPrice: data.productSku.marketPrice || 0,
          stockNum: data.productSku.stockNum || 0
        }
      }
      
      // 设置轮播图
      if (data.sliderUrlList && Array.isArray(data.sliderUrlList)) {
        sliderUrlList.value = data.sliderUrlList
      } else if (productSku.value.thumbImg) {
        sliderUrlList.value = [productSku.value.thumbImg]
      }
      
      // 设置详情图
      if (data.detailsImageUrlList && Array.isArray(data.detailsImageUrlList)) {
        detailsImageUrlList.value = data.detailsImageUrlList
      }
      
      // 设置规格
      if (data.specValueList && Array.isArray(data.specValueList)) {
        specValueList.value = data.specValueList
        // 初始化选中的规格
        data.specValueList.forEach(spec => {
          if (spec.key && spec.valueList && spec.valueList.length > 0) {
            selectedSpec[spec.key] = spec.valueList[0]
          }
        })
      }
      
      // 记录浏览历史
      try {
        await recordBrowse(Number(skuId))
      } catch (error) {
        // 不阻塞页面，静默失败
      }
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
}

const selectSpec = (key, value) => {
  selectedSpec[key] = value
}

const addCart = async () => {
  try {
    // 调用后端 API 添加商品到购物车
    await addToCart(skuId, buyNum.value)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败，请重试')
  }
}

// 立即购买 - 跳转到订单确认页
const openBuyModal = () => {
  const hasEmptySpec = Object.values(selectedSpec).some(val => !val)
  if (hasEmptySpec) {
    ElMessage.warning('请先选择完整的商品规格！')
    return
  }

  // 组装商品数据
  const orderGoods = [{
    name: product.value.name,
    image: productSku.value.thumbImg,
    specs: { ...selectedSpec },
    price: productSku.value.salePrice,
    num: buyNum.value
  }]

  // 跳转到订单确认页（传递skuId，从后端获取商品信息）
  router.push({
    path: '/order/confirm',
    query: {
      skuId: skuId
    }
  })
}

onMounted(async () => {
  checkMobile()
  // 加载商品详情
  await loadProductDetail()
  // 初始化收藏状态
  if (skuId) {
    isCollected.value = await checkIsCollected(Number(skuId))
  }
})
</script>

<style scoped>
@import "@/assets/css/detail.css";
</style>
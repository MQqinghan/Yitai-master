<template>
  <div class="home-container">
    <!-- 搜索栏：接收isPc -->
    <SearchBar
        :isPc="isPc"
        :initKeyword="keyword"
        :openNewWindow="isPc" />

    <!-- PC端布局 -->
    <div class="pc-layout" v-if="isPc">
      <div class="ad-banner-section">
        <div class="category-column">
          <el-menu
              default-active="1"
              class="category-menu"
              background-color="#f5f5f5"
              text-color="#333"
              active-text-color="#409eff"
              @select="handlePcCategoryClick"
          > <!-- PC分类点击事件 -->
          <el-menu-item index="1">数码</el-menu-item>
          <el-menu-item index="2">家居</el-menu-item>
          <el-menu-item index="3">穿搭</el-menu-item>
          <el-menu-item index="4">食品</el-menu-item>
          <el-menu-item index="5">户外</el-menu-item>
          <el-menu-item index="6">美妆</el-menu-item>
          <el-menu-item index="7">运动</el-menu-item>
          <el-menu-item index="8">学习</el-menu-item>
          <el-menu-item index="9">医药</el-menu-item>
          <el-menu-item index="10">玩具</el-menu-item>
          </el-menu>
        </div>
        <div class="ad-banner">
          <el-carousel height="400px" indicator-position="none" autoplay>
            <el-carousel-item v-for="(item, index) in adBanners" :key="index">
              <img :src="item.imgUrl" alt="广告轮播图" class="banner-img" />
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="right-boxes">
          <div class="box-item" v-for="(item, index) in rightBoxes" :key="index">
            <img :src="item.imgUrl" alt="侧边盒子" class="box-img" />
            <p class="box-text">{{ item.text }}</p>
          </div>
        </div>
      </div>
      <div class="hot-sales-section">
        <h3 class="section-title">畅销商品</h3>
        <el-carousel height="300px" arrow="hover">
          <!-- 轮播项：每个item里放3个商品，加flex容器 -->
          <el-carousel-item v-for="(item, index) in hotSales" :key="index">
            <div class="product-card-container"> <!-- 新增容器：控制一行3个 -->
              <div class="product-card" @click="handleProductClick(item.id)">
                <img :src="item.imgUrl" alt="畅销商品" class="product-img" />
                <p class="product-name">{{ item.name }}</p>
                <span class="product-price">¥{{ item.price }}</span>
              </div>
              <div class="product-card" @click="handleProductClick(item.id)">
                <img :src="item.imgUrl" alt="畅销商品" class="product-img" />
                <p class="product-name">{{ item.name }}</p>
                <span class="product-price">¥{{ item.price }}</span>
              </div>
              <div class="product-card" @click="handleProductClick(item.id)">
                <img :src="item.imgUrl" alt="畅销商品" class="product-img" />
                <p class="product-name">{{ item.name }}</p>
                <span class="product-price">¥{{ item.price }}</span>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <!-- 移动端布局 -->
    <div class="mobile-layout" v-else>
      <div class="mobile-category">
        <div class="category-scroll">
          <div class="category-item" v-for="(item, index) in mobileCategories" :key="index" @click="handleCategoryClick(item)">
            <div class="category-icon">
              <img :src="item.icon" :alt="item.name" />
            </div>
            <span class="category-name">{{ item.name }}</span>
          </div>
        </div>
      </div>
      <div class="mobile-hot-sales">
        <h3 class="section-title">热销商品</h3>
        <div class="product-list">
          <div class="mobile-product-card"
               v-for="(item, index) in hotSales"
               :key="index"
               @click="handleProductClick(item.id)"
          >
            <img :src="item.imgUrl" alt="热销商品" class="mobile-product-img" />
            <p class="mobile-product-name">{{ item.name }}</p>
            <span class="mobile-product-price">¥{{ item.price }}</span>
          </div>
        </div>
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

  <!-- 返回顶部按钮 -->
  <div
      class="go-top"
      v-show="showGoTop"
      @click="goToTop"
  >
    <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2" fill="none">
      <path d="M18 15l-6-6-6 6"/>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import SearchBar from "@/components/searchBar.vue";
import {useDevice} from "@/hooks/useDevice"
import {useGoDetail} from "@/hooks/useGoDetail";
import { useRoute } from 'vue-router'
import { useScrollTop } from '@/hooks/useScrollTop'
import { getBestsellerList, getGuessLikeList, getIndexData } from '@/api/product' // 导入 API
const { showGoTop, goToTop } = useScrollTop()


//获取响应式的isPc
const {isPc} = useDevice();
const router = useRouter();
const route = useRoute();
//获取点击商品的方法
const {handleProductClick} = useGoDetail();
const skuId = route.params.skuId
// 模拟数据
const adBanners = ref([
  { imgUrl: 'https://picsum.photos/800/400?random=1', text: '广告1' },
  { imgUrl: 'https://picsum.photos/800/400?random=2', text: '广告2' },
  { imgUrl: 'https://picsum.photos/800/400?random=3', text: '广告3' },
]);
const rightBoxes = ref([
  { imgUrl: 'https://picsum.photos/150/120?random=4', text: '限时特惠' },
  { imgUrl: 'https://picsum.photos/150/120?random=5', text: '新品上市' },
  { imgUrl: 'https://picsum.photos/150/120?random=6', text: '会员专享' },
]);
const hotSales = ref<any[]>([]);
const mobileCategories = ref([
  { name: '数码', icon: 'https://picsum.photos/60/60?random=1' },
  { name: '家居', icon: 'https://picsum.photos/60/60?random=2' },
  { name: '穿搭', icon: 'https://picsum.photos/60/60?random=3' },
  { name: '食品', icon: 'https://picsum.photos/60/60?random=4' },
  { name: '户外', icon: 'https://picsum.photos/60/60?random=5' },
  { name: '美妆', icon: 'https://picsum.photos/60/60?random=6' },
  { name: '运动', icon: 'https://picsum.photos/60/60?random=7' },
  { name: '学习', icon: 'https://picsum.photos/60/60?random=8' },
  { name: '医药', icon: 'https://picsum.photos/60/60?random=9' },
  { name: '玩具', icon: 'https://picsum.photos/60/60?random=10' },
]);
const guessLikeList = ref<any[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const loading = ref(false);

// 通用跳转函数，区分PC/移动端
const navigateToSearch = (keyword: string) => {
  if (!keyword.trim()) return;

  // PC端：打开新窗口；移动端：当前页跳转
  if (isPc.value) {
    const targetUrl = router.resolve({
      name: 'searchResult',
      query: { keyword: keyword.trim() }
    }).href;
    window.open(targetUrl, '_blank');
  } else {
    // 移动端：当前页替换路由，不打开新窗口
    router.push({
      name: 'searchResult',
      query: { keyword: keyword.trim() }
    });
  }
};

// 移动端分类点击
const handleCategoryClick = (item: { name: string; icon: string }) => {
  navigateToSearch(item.name);
};

// PC 端分类点击
const handlePcCategoryClick = (index: string) => {
  const categoryMap = {
    '1': '数码', '2': '家居', '3': '穿搭', '4': '食品', '5': '户外',
    '6': '美妆', '7': '运动', '8': '学习', '9': '医药', '10': '玩具'
  };
  const categoryName = categoryMap[index] || '';
  navigateToSearch(categoryName);
};

// 加载畅销商品数据
const loadBestsellers = async () => {
  try {
    const res = await getIndexData(); // 使用新的首页接口
    // 后端返回的 IndexVo 包含 productSkuList
    if (res.data && res.data.productSkuList) {
      // 将后端返回的商品数据映射到前端需要的格式
      hotSales.value = res.data.productSkuList.map((item: any) => ({
        id: item.id,
        name: item.skuName || item.name,
        price: item.price || item.salePrice,
        imgUrl: item.thumbImg || item.image,
        sales: item.saleNum // 销量
      }));
    }
  } catch (error: any) {
    console.error('获取畅销商品失败:', error.message);
    ElMessage.error('加载商品数据失败');
  }
};

// 加载猜你喜欢数据
const loadGuessLike = async (isLoadMore = false) => {
  if (isLoadMore && !hasMore.value) return;
  
  // 限制最大加载页数，防止无限加载
  const maxPage = 10; // 最多加载10页
  if (currentPage.value > maxPage) {
    hasMore.value = false;
    return;
  }
  
  loading.value = true;
  try {
    const res = await getGuessLikeList(currentPage.value, pageSize.value);
    if (res.data) {
      const newList = res.data.map((item: any) => ({
        id: item.id,
        name: item.skuName || item.name,
        price: item.price || item.salePrice,
        imgUrl: item.thumbImg || item.image,
        sales: item.saleNum
      }));
      
      if (isLoadMore) {
        // 去重：过滤掉已经存在的商品
        const existingIds = new Set(guessLikeList.value.map(item => item.id));
        const uniqueNewList = newList.filter(item => !existingIds.has(item.id));
        guessLikeList.value = [...guessLikeList.value, ...uniqueNewList];
        
        // 如果没有新增不重复的商品，说明已经加载完了
        if (uniqueNewList.length === 0) {
          hasMore.value = false;
        }
      } else {
        guessLikeList.value = newList;
      }
      
      // 如果返回的数据少于pageSize，说明没有更多数据了
      if (newList.length < pageSize.value) {
        hasMore.value = false;
      }
    } else {
      hasMore.value = false;
    }
  } catch (error: any) {
    console.error('获取推荐商品失败:', error.message);
    if (!isLoadMore) {
      ElMessage.error('加载推荐商品失败');
    }
  } finally {
    loading.value = false;
  }
};

// 滚动事件处理
const handleScroll = () => {
  // 当滚动到距离底部200px时触发加载
  const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight;
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
  const clientHeight = document.documentElement.clientHeight || document.body.clientHeight;
  
  if (scrollHeight - scrollTop - clientHeight < 200) {
    if (!loading.value && hasMore.value) {
      currentPage.value++;
      loadGuessLike(true);
    }
  }
};

onMounted(() => {
  document.oncontextmenu = null; // 恢复右键
  
  // 加载后端数据
  loadBestsellers();
  loadGuessLike(); // 临时使用首页数据
  
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
@import "@/assets/css/content.css";
@import "@/assets/css/goTop.css";
</style>
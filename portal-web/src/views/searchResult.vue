<template>
  <div class="search-result-container">

    <!-- 复用全局搜索栏组件 -->
    <SearchBar
        :isPc="isPc"
        :initKeyword="keyword"
        :openNewWindow="false"
        :showBackBtn="true"
    />

    <!-- 筛选提示 -->
    <div class="filter-tip">
      <span v-if="loading">搜索中...</span>
      <span v-else>搜索"{{ keyword }}"共找到 {{ total }} 件商品</span>
    </div>

    <!-- 商品列表（适配PC/移动端） -->
    <div class="product-list" ref="scrollContainer">
      <!-- 无商品时的空状态 -->
      <div class="empty-tip" v-if="!loading && filteredProducts.length === 0">
        <el-empty description="暂无相关商品" />
      </div>

      <!-- 商品卡片 -->
      <div
          class="product-card"
          v-for="(item, index) in filteredProducts"
          :key="index"
          @click="handleProductClick(item.id || item.skuId)"
      >
        <img :src="item.imgUrl || item.thumbImg || item.image" alt="商品图片" class="product-img" />
        <p class="product-name" v-html="item.productName || item.spuName || item.name || item.skuName || ''"></p>
        <div class="product-price">¥{{ item.salePrice || item.price }}</div>
        <el-button type="primary" size="small" class="add-cart-btn" @click.stop="addToCartHandler(item)">加入购物车</el-button>
      </div>
      
      <!-- 加载状态提示 -->
      <div class="loading-more" v-if="loading && filteredProducts.length > 0">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <!-- 加载完成提示 -->
      <div class="load-complete" v-if="!loading && filteredProducts.length > 0 && filteredProducts.length >= total">
        <span>— 已经到底了 —</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import SearchBar from "@/components/searchBar.vue"; // 导入全局搜索栏
import {useDevice} from "@/hooks/useDevice";
import { searchProduct } from '@/api/product'; // 导入搜索 API
import { addToCart } from '@/api/cart'; // 导入购物车 API
import { useGoDetail } from '@/hooks/useGoDetail'; // 导入商品详情跳转
import { useScrollTop } from '@/hooks/useScrollTop'; // 导入返回顶部 hook

// 接收路由参数、父组件传递的isPc
const route = useRoute();
const router = useRouter();

const {isPc} = useDevice();
const { handleProductClick } = useGoDetail(); // 获取商品详情跳转方法
const { showGoTop, goToTop } = useScrollTop(); // 获取返回顶部功能
const keyword = ref('');
const productList = ref<any[]>([]); // 搜索结果列表
const loading = ref(false); // 加载状态
const total = ref(0); // 总数量
const currentPage = ref(1); // 当前页码
const pageSize = ref(20); // 每页数量
const scrollContainer = ref<HTMLElement | null>(null); // 滚动容器引用
const hasMore = ref(true); // 是否还有更多数据

// 根据关键词筛选商品（忽略大小写，提升体验）
const filteredProducts = computed(() => {
  return productList.value;
});

// 加载搜索数据
const loadSearchData = async (isLoadMore = false) => {
  if (!keyword.value.trim()) {
    if (!isLoadMore) {
      productList.value = [];
      total.value = 0;
    }
    return;
  }
  
  // 如果没有更多数据，直接返回
  if (isLoadMore && !hasMore.value) return;

  loading.value = true;
  try {
    const res = await searchProduct({
      keyword: keyword.value.trim(),
      page: currentPage.value,
      limit: pageSize.value
    });
    
    // 后端返回的是 SearchResultVo 对象：{ list: [], total: number }
    if (res.data) {
      const newList = res.data.list || [];
      total.value = res.data.total || 0;
      
      // 调试：打印第一条数据的字段结构
      if (newList.length > 0 && !isLoadMore) {
        console.log('搜索结果第一条数据:', JSON.parse(JSON.stringify(newList[0])));
      }
      
      if (isLoadMore) {
        // 加载更多时追加数据
        productList.value = [...productList.value, ...newList];
      } else {
        // 首次加载时替换数据
        productList.value = newList;
      }
      
      // 判断是否还有更多数据
      hasMore.value = productList.value.length < total.value;
    }
  } catch (error: any) {
    console.error('搜索失败:', error.message);
    if (!isLoadMore) {
      ElMessage.error('搜索失败，请稍后重试');
      productList.value = [];
    }
  } finally {
    loading.value = false;
  }
};

// 加入购物车
const addToCartHandler = async (item: any) => {
  const skuId = item.id || item.skuId;
  if (!skuId) {
    ElMessage.warning('商品信息错误');
    return;
  }
  
  try {
    await addToCart(skuId, 1); // 默认添加1件
    ElMessage.success('已加入购物车');
  } catch (error: any) {
    console.error('加入购物车失败:', error.message);
    ElMessage.error(error.message || '加入购物车失败');
  }
};

// 无限滚动事件处理
const handleInfiniteScroll = () => {
  if (!scrollContainer.value) return;
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer.value;
  // 当滚动到距离底部200px时触发加载
  if (scrollHeight - scrollTop - clientHeight < 200) {
    if (!loading.value && hasMore.value) {
      currentPage.value++;
      loadSearchData(true);
    }
  }
};

// 页面加载时初始化
onMounted(() => {
  const initKeyword = route.query.keyword as string || '';
  if (initKeyword) {
    keyword.value = initKeyword;
    loadSearchData();
  }
  
  // 添加无限滚动监听
  window.addEventListener('scroll', handleInfiniteScroll);
});

onUnmounted(() => {
  // 移除无限滚动监听
  window.removeEventListener('scroll', handleInfiniteScroll);
});

watch(
    () => route.query.keyword, // 监听路由参数变化
    (newKeyword) => {
      if (newKeyword) {
        keyword.value = newKeyword as string;
        currentPage.value = 1; // 重置页码
        hasMore.value = true; // 重置加载状态
        productList.value = []; // 清空旧数据
        loadSearchData();
      }
    },
    { immediate: true } // 立即执行，确保首次加载也生效
);
</script>

<style scoped>
@import "@/assets/css/searchResult.css";
</style>

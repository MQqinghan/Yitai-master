<template>
  <div class="search-bar" :class="{ 'fixed': isFixed }">
    <div class="search-container">
      <!-- 移动端返回首页按钮 -->
      <el-button
          v-if="!isPc && showBackBtn"
          icon="ArrowLeft"
          class="back-home-btn"
          @click="goHome"
          type="text"
      ></el-button>

      <el-input
          v-model="searchText"
          placeholder="请输入关键词搜索"
          prefix-icon="Search"
          @keyup.enter="searchProduct"
          class="search-input"
      >
        <template #append>
          <el-button type="primary" class="search-button" @click="searchProduct">搜索</el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter} from 'vue-router';


// 接收父组件的isPc
const props = defineProps<{
  isPc: boolean;
  initKeyword?: string; // 初始化关键词
  openNewWindow?: boolean;//默认打开新窗口
  showBackBtn?: boolean; // 是否显示返回首页按钮
}>();
const router = useRouter();
const searchText = ref('');
const isFixed = ref(false);

//返回首页
const goHome = () => {
  router.push({ name: 'home' });
}


// 通用搜索跳转函
const searchProduct = () => {
  const keyword = searchText.value.trim();
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }
  const needOpenNew = props.openNewWindow ?? true;
  if (needOpenNew) {
    const targetUrl = router.resolve({
      name: 'searchResult',
      query: { keyword }
    }).href;
    const newWindow = window.open(targetUrl, '_blank');
    if (!newWindow || newWindow.closed) {
      ElMessage.error('新窗口被浏览器拦截，请允许弹窗后重试');
    }
    searchText.value = '';
  } else {
    router.replace({
      name: 'searchResult',
      query: { keyword }
    });
  }
};

// 滚动监听：PC端滚动到顶部固定，移动端始终固定
const handleScroll = () => {
  if (props.isPc) {
    isFixed.value = window.scrollY >= 60; // PC端导航高度60px
  }
};

// 新增：监听initKeyword变化，同步到搜索框
watch(
    () => props.initKeyword,
    (newVal) => {
      if (newVal) {
        searchText.value = newVal;
      }
    },
    { immediate: true } // 立即执行一次，确保初始化时生效
);
// 监听设备变化
watch(() => props.isPc, (newVal) => {
  if (!newVal) {
    isFixed.value = true; // 移动端强制固定
  } else {
    isFixed.value = window.scrollY >= 60; // PC端按滚动位置
  }
}, { immediate: true });

// 生命周期
onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  isFixed.value = !props.isPc || window.scrollY >= 60;

});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
@import '@/assets/css/searchBar.css';
</style>

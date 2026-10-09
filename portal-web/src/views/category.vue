<template>
  <div class="category-page">
    <!-- 顶部标题栏：左上角返回箭头 + 居中标题 -->
    <div class="page-header">
      <!-- 返回首页箭头 -->
      <el-button
          icon="ArrowLeft"
          class="back-home-btn"
          @click="goHome"
          type="text"
      ></el-button>
      <div class="page-title">分类</div>
    </div>

    <!-- 分类容器：左侧垂直一级分类 + 右侧内容区 -->
    <div class="category-container">
      <!-- 左侧：垂直一级分类（参考图样式） -->
      <div class="first-category-sidebar">
        <div
            class="first-item"
            :class="{ active: activeFirstIndex === index }"
            v-for="(item, index) in firstCategories"
            :key="item.id"
            @click="switchFirstCategory(index)"
        >
          {{ item.name }}
        </div>
      </div>

      <!-- 右侧：二级+三级分类内容区（参考图样式） -->
      <div class="category-content">
        <!-- 空状态提示 -->
        <div class="empty-tip" v-if="currentSecondCategories.length === 0">
          暂无该分类下的商品
        </div>

        <div class="second-category" v-for="second in currentSecondCategories" :key="second.id" @click="goToSearch(second.name)">
          <h3 class="second-title">{{ second.name }}</h3>
          <div class="third-category-list">
            <div class="third-item" v-for="third in second.children" :key="third.id" @click="goToSearch(third.name)">
              <img :src="third.imgUrl" :alt="third.name" class="third-img" />
              <span class="third-name">{{ third.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
const router = useRouter();
//返回首页
const goHome = () => {
  router.push({ name: 'home' });
}
// 一级分类模拟数据（10个）
const firstCategories = ref([
  { id: 1, name: '数码' },
  { id: 2, name: '家居' },
  { id: 3, name: '穿搭' },
  { id: 4, name: '食品' },
  { id: 5, name: '户外' },
  { id: 6, name: '美妆' },
  { id: 7, name: '运动' },
  { id: 8, name: '学习' },
  { id: 9, name: '医药' },
  { id: 10, name: '玩具' }
]);

// 二级+三级分类模拟数据
const categoryData = ref({
  1: [ // 数码
    {
      id: 11, name: '手机通讯',
      children: [
        { id: 111, name: '手机', imgUrl: 'https://picsum.photos/55/55?random=1' },
        { id: 112, name: '对讲机', imgUrl: 'https://picsum.photos/55/55?random=2' },
        { id: 113, name: '电话手表', imgUrl: 'https://picsum.photos/55/55?random=3' },
        { id: 114, name: '蓝牙耳机', imgUrl: 'https://picsum.photos/55/55?random=4' },
        { id: 115, name: '充电器', imgUrl: 'https://picsum.photos/55/55?random=5' }
      ]
    },
    {
      id: 12, name: '摄影摄像',
      children: [
        { id: 121, name: '数码相机', imgUrl: 'https://picsum.photos/55/55?random=6' },
        { id: 122, name: '单反相机', imgUrl: 'https://picsum.photos/55/55?random=7' },
        { id: 123, name: '镜头', imgUrl: 'https://picsum.photos/55/55?random=8' },
        { id: 124, name: '摄像机', imgUrl: 'https://picsum.photos/55/55?random=9' }
      ]
    },
    {
      id: 13, name: '电脑',
      children: [
        { id: 131, name: '华硕', imgUrl: 'https://picsum.photos/55/55?random=6' },
        { id: 132, name: '神舟', imgUrl: 'https://picsum.photos/55/55?random=7' },
        { id: 133, name: '宏碁', imgUrl: 'https://picsum.photos/55/55?random=8' }
      ]
    }
  ],
  2: [ // 家居
    {
      id: 21, name: '客厅家具',
      children: [
        { id: 211, name: '沙发', imgUrl: 'https://picsum.photos/55/55?random=10' },
        { id: 212, name: '茶几', imgUrl: 'https://picsum.photos/55/55?random=11' },
        { id: 213, name: '电视柜', imgUrl: 'https://picsum.photos/55/55?random=12' }
      ]
    }
  ],
  3: [], 4: [], 5: [], 6: [], 7: [], 8: [], 9: [], 10: []
});

// 当前选中的一级分类索引
const activeFirstIndex = ref(0);

// 计算当前选中一级分类下的二级+三级分类
const currentSecondCategories = computed(() => {
  const firstId = firstCategories.value[activeFirstIndex.value].id;
  return categoryData.value[firstId] || [];
});

// 切换一级分类
const switchFirstCategory = (index: number) => {
  activeFirstIndex.value = index;
  document.querySelector('.category-content')?.scrollTo(0, 0);
};

// 返回首页（替换为你的首页路由name）
const goBackHome = () => {
  router.push({ name: 'home' });
};

// 二、三级分类点击跳转搜索页
const goToSearch = (keyword: string) => {
  router.push({
    name: 'searchResult',
    query: { keyword }
  });
};
</script>

<style scoped>
@import "@/assets/css/category.css";
</style>
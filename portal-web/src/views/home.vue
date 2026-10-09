<template>
  <!-- 核心修复：把三元运算拆成变量，避免行内语法报错 -->
  <div class="home-page" :style="{ paddingTop: paddingTopValue }">
    <TopNav v-if="isPc" />
    <BottomNav v-else />
    <Content/>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import Content from "@/components/home/content.vue";
import BottomNav from "@/components/home/bottomNav.vue";
import TopNav from "@/components/home/topNav.vue";

// 1. 统一设备判断
const isPc = ref(window.innerWidth >= 768);
const checkDevice = () => {
  isPc.value = window.innerWidth >= 768;
};

// 2. 用computed计算padding值（核心：避免行内写三元运算）
const paddingTopValue = computed(() => {
  return isPc.value ? '0px' : '60px';
});

// 3. 生命周期
onMounted(() => {
  checkDevice();
  window.addEventListener('resize', checkDevice);
  document.oncontextmenu = null; // 恢复右键
});

onUnmounted(() => {
  window.removeEventListener('resize', checkDevice);
});
</script>

<style scoped>
.home-page {
  width: 100%;
  min-height: 100vh;
  background-color: #fff;
}
</style>
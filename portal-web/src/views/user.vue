=<template>
  <TopNav v-if="isPc" />
  <BottomNav v-else />
  <My/>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import My from "@/components/user/my.vue";
import BottomNav from "@/components/home/bottomNav.vue";
import TopNav from "@/components/home/topNav.vue";

// 设备判断
const isPc = ref(window.innerWidth >= 768);
const checkDevice = () => {
  isPc.value = window.innerWidth >= 768;
};

onMounted(() => {
  checkDevice();
  window.addEventListener('resize', checkDevice);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkDevice);
});
</script>

<style scoped>

</style>
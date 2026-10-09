<template>
  <div class="mobile-nav">
    <div class="nav-container">
      <router-link to="/home" class="nav-item" active-class="active">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </router-link>
      <router-link to="/category" class="nav-item" active-class="active">
        <el-icon><Promotion /></el-icon>
        <span>分类</span>
      </router-link>
      <router-link to="/message" class="nav-item" active-class="active">
        <el-icon><Message /></el-icon>
        <span>消息</span>
      </router-link>
      <router-link to="/cart" class="nav-item" active-class="active">
        <el-icon><ShoppingCart /></el-icon>
        <span>购物车</span>
      </router-link>
      <div @click="goUser" class="nav-item" active-class="active">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { House, Promotion, Message, ShoppingCart, User } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
const router = useRouter();

const goUser = () => {
  router.push('/user')
}
</script>

<style scoped>
@import "@/assets/css/nav.css";
</style>
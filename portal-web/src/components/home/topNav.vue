<template>
  <div class="pc-nav">
    <div class="nav-container">
      <div class="nav-left">
        <template v-if="isLoggedIn">
          <div class="user-info-display">
            <img :src="userInfo.avatar" alt="头像" class="user-avatar" />
            <span class="user-name">{{ userInfo.nickName }}</span>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <span class="separator">/</span>
          <router-link to="/register" class="nav-link">注册</router-link>
        </template>
      </div>
      <div class="nav-right">
        <div @click="goUser" class="nav-item">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </div>
        <div @click="goCart" class="nav-item">
          <el-icon><ShoppingCart /></el-icon>
          <span>购物车</span>
        </div>
        <div @click="goCollection"  class="nav-item">
          <el-icon><Star /></el-icon>
          <span>收藏夹</span>
        </div>
        <router-link to="/help" class="nav-item">
          <el-icon><QuestionFilled /></el-icon>
          <span>帮助</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { User, ShoppingCart, Star, QuestionFilled } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { ref, onMounted } from 'vue';
import { loadUserInfo, userInfo, isLoggedIn } from '@/hooks/useUser';

const router = useRouter();

// 加载用户信息
onMounted(() => {
  loadUserInfo();
});

const goCart = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/cart')
  } else {
    router.push('/login')
  }
}
const goUser = () => {
  router.push('/user')
}
const goCollection = () => {
  router.push('/collection')
}
</script>

<style scoped>
@import "@/assets/css/nav.css";
</style>
<template>
  <!-- 路由出口：所有页面都会渲染到这里 -->
  <router-view></router-view>
  <!-- 全局 AI 助手悬浮窗（智能导购 / 智能客服） -->
  <AiAssistant />
</template>

<script setup>
import AiAssistant from '@/components/ai/AiAssistant.vue'
// 无需额外代码，仅作为路由容器
</script>

<style>
/* 全局样式，清除默认边距 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>

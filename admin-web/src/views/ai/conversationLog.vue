<template>
  <div class="app-container">
    <el-card>
      <div class="head">
        <h2>AI 会话记录</h2>
        <el-radio-group v-model="mode" @change="load">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="shopping">智能导购</el-radio-button>
          <el-radio-button label="service">智能客服</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.mode === 'shopping' ? 'warning' : 'success'">
              {{ row.mode === 'shopping' ? '导购' : '客服' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userMessage" label="用户提问" show-overflow-tooltip />
        <el-table-column prop="botAnswer" label="AI 回答" show-overflow-tooltip />
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无会话记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getConversations } from '@/api/ai'

const list = ref([])
const loading = ref(false)
const mode = ref('')

async function load() {
  loading.value = true
  try {
    const res = await getConversations({ mode: mode.value || undefined, limit: 100 })
    list.value = (res && res.data) || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.head h2 {
  font-size: 18px;
  margin: 0;
}
</style>

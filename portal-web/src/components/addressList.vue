<template>
  <div class="address-list-page">
    <!-- 顶部导航栏（移动端返回+标题，PC端可隐藏返回） -->
    <div class="address-header">
      <el-icon class="back-icon" @click="goBack" v-show="isMobile">
        <ArrowLeft />
      </el-icon>
      <h2 class="page-title">收货地址</h2>
    </div>

    <!-- 地址列表 -->
    <div class="address-container">
      <div
          class="address-item"
          v-for="addr in addressList"
          :key="addr.id"
          :class="{ 'default-item': addr.isDefault }"
      >
        <div class="address-info">
          <div class="addr-top">
            <span class="name">{{ addr.name }}</span>
            <span class="phone">{{ addr.phone }}</span>
            <el-tag v-if="addr.lastUsed" type="danger" size="small" class="last-used-tag">
              上次使用
            </el-tag>
          </div>
          <div class="addr-detail">
            {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
          </div>
        </div>
        <div class="address-select">
          <el-icon v-if="addr.isDefault" class="check-icon" color="#ff4d4f">
            <Check />
          </el-icon>
          <el-button
              v-else
              size="small"
              class="use-btn"
              @click="selectAddress(addr)"
          >
            使用
          </el-button>
          <el-icon class="del-icon" @click="deleteAddress(addr.id)">
            <Close />
          </el-icon>
        </div>
        <div class="addr-actions">
          <div class="default-radio">
            <el-radio v-model="defaultAddressId" :label="addr.id" @change="setDefault(addr.id)">
              {{ addr.isDefault ? '已设默认' : '默认' }}
            </el-radio>
          </div>
          <div class="action-btns">
            <span class="action-btn" @click="pinAddress(addr.id)">置顶</span>
            <span class="divider">|</span>
            <span class="action-btn" @click="copyAddress(addr)">复制</span>
            <span class="divider">|</span>
            <span class="action-btn" @click="editAddress(addr)">修改</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加新地址按钮（底部固定） -->
    <div class="add-address-bar" @click="goToAddAddress">
      <el-icon class="add-icon"><Plus /></el-icon>
      <span>添加收货地址</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ArrowLeft, Check, Close, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 判断是否移动端
const isMobile = ref(window.innerWidth < 768)
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 768
})

// 模拟地址数据
const addressList = ref([
  {
    id: 1,
    name: '黄泉',
    phone: '19313859951',
    province: '湖北省',
    city: '武汉市',
    district: '武昌区',
    detail: '湖北大学二期公寓菜鸟驿站',
    isDefault: true,
    lastUsed: true
  },
  {
    id: 2,
    name: '黄泉',
    phone: '19313859951',
    province: '广东省',
    city: '河源市',
    district: '源城区',
    detail: '新江三路北一巷58号',
    isDefault: false,
    lastUsed: false
  }
])

// 默认地址ID
const defaultAddressId = ref(1)

// 计算默认地址
const defaultAddress = computed(() =>
    addressList.value.find(addr => addr.id === defaultAddressId.value)
)

// 返回上一页
const goBack = () => {
    router.back()
}


// 选择地址（返回订单确认页）
const selectAddress = (addr) => {
  if (route.query.from === 'buy') {
    // 保存选中的地址
    localStorage.setItem('selectedAddressId', addr.id)
    // 自动返回上一页（订单确认页）
    router.back()
    return
  }
}

// 设置默认地址
const setDefault = (id) => {
  addressList.value.forEach(addr => {
    addr.isDefault = addr.id === id
  })
  ElMessage.success('已设为默认地址')
}

// 删除地址
const deleteAddress = (id) => {
  ElMessageBox.confirm('确定删除该地址吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    addressList.value = addressList.value.filter(addr => addr.id !== id)
    ElMessage.success('删除成功')
  })
}

// 置顶地址（模拟）
const pinAddress = (id) => {
  ElMessage.info('置顶功能开发中')
}

// 复制地址
const copyAddress = (addr) => {
  const text = `${addr.name} ${addr.phone} ${addr.province}${addr.city}${addr.district}${addr.detail}`
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('地址已复制')
  })
}

// 编辑地址（模拟）
const editAddress = (addr) => {
  ElMessage.info('编辑地址功能开发中')
}

// 跳转到添加地址页（模拟）
const goToAddAddress = () => {
  ElMessage.info('添加地址功能开发中')
}

onMounted(() => {
  // 如果从购买页跳转来，可自动选择上次使用地址
  if (route.query.from === 'buy') {
    const lastUsed = addressList.value.find(addr => addr.lastUsed)
    if (lastUsed) {
      defaultAddressId.value = lastUsed.id
    }
  }
})
</script>

<style scoped>
@import "@/assets/css/addressList.css";
</style>
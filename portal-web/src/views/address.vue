<template>
  <div class="address-page">
    <!-- 顶部导航栏 -->
    <div class="address-header">
      <el-icon class="back-icon" @click="goBack" v-show="isMobile || isSelectionMode">
        <ArrowLeft />
      </el-icon>
      <h2 class="page-title">{{ isSelectionMode ? '选择收货地址' : '收货地址' }}</h2>
    </div>

    <!-- 地址列表 -->
    <div class="address-container">
      <div
          class="address-item"
          v-for="addr in addressList"
          :key="addr.id"
          :class="{ 'default-item': addr.isDefault === 1, 'selectable': isSelectionMode }"
      >
        <div class="address-info" @click="selectAddress(addr)">
          <div class="addr-top">
            <span class="name">{{ addr.name }}</span>
            <span class="phone">{{ addr.phone }}</span>
            <el-tag v-if="addr.isDefault === 1" type="danger" size="small" class="default-tag">
              默认
            </el-tag>
            <el-tag v-if="isSelectionMode" type="success" size="small" class="use-tag">
              点击选择
            </el-tag>
          </div>
          <div class="addr-detail">
            {{ addr.fullAddress }}
          </div>
        </div>
        
        <!-- 管理模式：显示编辑、删除、设为默认 -->
        <div class="address-actions" v-if="!isSelectionMode">
          <div class="action-btns">
            <span class="action-btn" @click="editAddress(addr)">编辑</span>
            <span class="divider">|</span>
            <span class="action-btn" @click="deleteAddress(addr.id)">删除</span>
            <span class="divider">|</span>
            <span
                class="action-btn"
                :class="{ 'set-default': addr.isDefault !== 1 }"
                @click="setDefault(addr.id)"
            >
              {{ addr.isDefault === 1 ? '已默认' : '设为默认' }}
            </span>
          </div>
        </div>
        
        <!-- 选择模式：显示使用按钮 -->
        <div class="address-select" v-else>
          <el-icon v-if="addr.isDefault === 1" class="check-icon" color="#ff4d4f">
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
          <el-icon class="del-icon" @click="goBack">
            <Close />
          </el-icon>
        </div>
      </div>

      <!-- 空状态提示 -->
      <div class="empty-state" v-if="addressList.length === 0">
        <el-icon class="empty-icon"><Location /></el-icon>
        <p class="empty-text">暂无收货地址</p>
      </div>
    </div>

    <!-- 添加新地址按钮（管理模式显示） -->
    <div class="add-address-bar" @click="showAddDialog = true" v-if="!isSelectionMode">
      <el-icon class="add-icon"><Plus /></el-icon>
      <span>添加收货地址</span>
    </div>

    <!-- 添加/编辑地址弹窗 -->
    <el-dialog
        v-model="showAddDialog"
        :title="isEdit ? '编辑地址' : '添加地址'"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form :model="addressForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所在地区" prop="fullAddress">
          <el-input v-model="addressForm.fullAddress" placeholder="如：湖北省 武汉市 武昌区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="addressForm.address" type="textarea" placeholder="请输入详细地址" :rows="3" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="submitAddress">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Location } from '@element-plus/icons-vue'
import { getAddressList, saveAddress, updateAddress, deleteAddress as deleteAddressApi, getAddressById } from '@/api/address'

const router = useRouter()
const route = useRoute()

// 判断是否为选择模式（从订单页进入）
const isSelectionMode = computed(() => route.query.from === 'buy')

// 地址列表
const addressList = ref([])
const loading = ref(false)

// 弹窗控制
const showAddDialog = ref(false)
const isEdit = ref(false)
const currentEditId = ref(null)
const formRef = ref(null)

// 表单数据
const addressForm = reactive({
  name: '',
  phone: '',
  fullAddress: '',
  address: '',
  isDefault: 0,
  tagName: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  fullAddress: [
    { required: true, message: '请输入所在地区', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 加载地址列表
const loadAddressList = async () => {
  loading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
  } catch (error) {
    console.error('加载地址列表失败:', error)
    ElMessage.error('加载地址列表失败')
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 选择地址（选择模式下使用）
const selectAddress = (addr) => {
  if (isSelectionMode.value) {
    // 保存选中的地址
    localStorage.setItem('selectedAddressId', addr.id)
    // 自动返回上一页（订单确认页）
    router.back()
  }
}

// 设为默认地址
const setDefault = async (id) => {
  // 选择模式下不允许修改默认地址
  if (isSelectionMode.value) {
    ElMessage.warning('请选择地址后返回')
    return
  }
  
  try {
    // 直接从本地地址列表获取地址信息
    const addressDetail = addressList.value.find(a => a.id === id)
    if (!addressDetail) {
      ElMessage.error('地址不存在')
      return
    }
    
    // 调用更新接口，将 isDefault 设为 1
    await updateAddress({
      id: addressDetail.id,
      name: addressDetail.name,
      phone: addressDetail.phone,
      tagName: addressDetail.tagName || '',
      provinceCode: addressDetail.provinceCode,
      cityCode: addressDetail.cityCode,
      districtCode: addressDetail.districtCode,
      address: addressDetail.address,
      fullAddress: addressDetail.fullAddress,
      isDefault: 1
    })
    
    ElMessage.success('已设为默认地址')
    await loadAddressList()
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}

// 删除地址
const deleteAddress = (id) => {
  ElMessageBox.confirm('确定删除该地址吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAddressApi(id)
      ElMessage.success('删除成功')
      await loadAddressList()
    } catch (error) {
      console.error('删除地址失败:', error)
      ElMessage.error('删除地址失败')
    }
  })
}

// 编辑地址
const editAddress = (addr) => {
  isEdit.value = true
  currentEditId.value = addr.id
  addressForm.name = addr.name
  addressForm.phone = addr.phone
  addressForm.fullAddress = addr.fullAddress
  addressForm.address = addr.address
  addressForm.isDefault = addr.isDefault
  addressForm.tagName = addr.tagName || ''
  showAddDialog.value = true
}

// 提交地址
const submitAddress = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      // 更新地址
      await updateAddress({
        id: currentEditId.value,
        ...addressForm
      })
      ElMessage.success('地址更新成功')
    } else {
      // 新增地址
      await saveAddress({
        ...addressForm
      })
      ElMessage.success('地址添加成功')
    }
    
    // 重新加载地址列表
    await loadAddressList()
    
    // 重置表单
    resetForm()
    showAddDialog.value = false
  } catch (error) {
    if (error !== false) {
      console.error('提交地址失败:', error)
      ElMessage.error('提交地址失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  isEdit.value = false
  currentEditId.value = null
  addressForm.name = ''
  addressForm.phone = ''
  addressForm.fullAddress = ''
  addressForm.address = ''
  addressForm.isDefault = 0
  addressForm.tagName = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

onMounted(() => {
  loadAddressList()
})
</script>

<style scoped>
@import '@/assets/css/address.css';
</style>

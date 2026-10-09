import { ref, computed } from 'vue'
import { getUserInfo as fetchUserInfo } from '@/api/user'

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 用户信息状态
const userInfo = ref({
  nickName: '',
  avatar: ''
})

// 是否已登录
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 获取用户信息
export const loadUserInfo = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    userInfo.value = {
      nickName: '',
      avatar: ''
    }
    return
  }

  try {
    const res = await fetchUserInfo()
    // res 已经是响应数据（响应拦截器处理过了）
    userInfo.value = {
      nickName: res.data?.nickName || '用户',
      avatar: res.data?.avatar || defaultAvatar
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    userInfo.value = {
      nickName: '',
      avatar: ''
    }
  }
}

// 清除用户信息（退出登录时使用）
export const clearUserInfo = () => {
  userInfo.value = {
    nickName: '',
    avatar: ''
  }
  localStorage.removeItem('token')
}

// 具名导出
export { userInfo, isLoggedIn }

// 默认导出
export default {
  userInfo,
  isLoggedIn,
  loadUserInfo,
  clearUserInfo
}

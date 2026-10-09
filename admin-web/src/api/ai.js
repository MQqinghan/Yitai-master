import axios from 'axios'
import { useApp } from '@/pinia/modules/app'
import router from '@/router'

// 直连网关(8500)，走 /api/ai/** 路由到 service-ai
const service = axios.create({
  baseURL: 'http://localhost:8500',
  timeout: 10000,
  // 通过 token 请求头鉴权即可，无需携带 cookie 凭证（规避网关 CORS 凭证限制）
  withCredentials: false
})

service.interceptors.request.use(
  (config) => {
    const { authorization } = useApp()
    if (authorization) {
      config.headers.token = `${authorization.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code == 208) {
      router.push(`/login?redirect=${encodeURIComponent(window.location.href)}`)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => Promise.reject(error)
)

// 获取 AI 会话记录
// 注意：路径不能含 /auth/ —— 网关对 /api/**/auth/** 按 C 端用户 Redis key 鉴权，
// 管理端 token 会被 208 拦截；演示环境此接口与 /chat 同级公开
export function getConversations(params) {
  return service({ url: '/api/ai/admin/conversations', method: 'get', params })
}

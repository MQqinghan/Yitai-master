import axios from 'axios'

const service = axios.create({
    baseURL: '/api',
    timeout: 5000
})

// 请求拦截器：后端要的是 token 头！
service.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.token = token
    }
    return config
})

service.interceptors.response.use(
    res => {
        // Axios 响应对象结构：{ data: { code, message, data }, status, statusText, headers, config }
        // 需要从 res.data 中获取后端返回的业务数据
        const responseData = res.data;
        
        // 根据 code 判断是否成功（常见的是 200 表示成功）
        if (responseData.code === 200 || responseData.code === 1 || responseData.code === '200' || responseData.code === '1') {
            return responseData  // 返回业务数据部分
        } else {
            // 业务错误，抛出异常
            const errorMsg = responseData.message || responseData.msg || '操作失败'
            return Promise.reject(new Error(errorMsg))
        }
    },
    err => {
        // 统一错误处理
        if (err.response) {
            switch (err.response.status) {
                case 401:
                    // 未授权，清除 token
                    localStorage.removeItem('token')
                    break
                case 403:
                    console.error('拒绝访问')
                    break
                case 404:
                    console.error('请求资源不存在')
                    break
                case 500:
                    console.error('服务器错误')
                    break
            }
        } else if (err.request) {
            console.error('网络错误，请检查网络连接')
        }
        
        return Promise.reject(err)
    }
)
export default service
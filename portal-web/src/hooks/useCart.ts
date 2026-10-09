import { useRouter } from 'vue-router'

// 判断是否登录
const isLogin = () => {
    // 从本地存储拿 token（你项目里存的啥名字就用啥）
    const token = localStorage.getItem('token')
    return !!token  // 有 = true，没有 = false
}

// 点击购物车
const goCart = () => {
    const router = useRouter()

    if (isLogin()) {
        // 已登录 → 去购物车
        router.push('/cart')
    } else {
        // 未登录 → 去登录
        router.push('/login')
    }
}
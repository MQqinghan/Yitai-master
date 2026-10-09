import { useRouter } from 'vue-router'

/**
 * 返回首页 Hook
 */
export const useGoHome = () => {
    const router = useRouter()

    /**
     * 跳转到首页
     */
    const goHome = () => {
        router.push({ path: '/' }) // 你的首页路由如果不是 /，就改成你的真实路径
    }

    return {
        goHome
    }
}
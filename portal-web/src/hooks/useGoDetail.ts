import { computed } from 'vue'
import { useRouter } from 'vue-router'

// 通用页面跳转 Hook
export function useGoDetail() {
    const router = useRouter()

    // 判断是否为 PC 端（和你项目逻辑保持一致）
    const isPc = computed(() => {
        return window.innerWidth >= 768
    })

    // 跳转商品详情页（PC新窗口 / 移动端当前页）
    const navigateToProductDetail = (skuId) => {
        if (!skuId) return

        if (isPc.value) {
            // PC：打开新窗口
            const targetUrl = router.resolve({
                name: 'detail',
                params: { skuId }
            }).href
            window.open(targetUrl, '_blank')
        } else {
            // 移动端：当前页跳转
            router.push({
                name: 'detail',
                params: { skuId }
            })
        }
    }
    // 点击商品触发跳转
    const handleProductClick = (skuId) => {
        navigateToProductDetail(skuId)
    }

    return {
        navigateToProductDetail,
        handleProductClick
    }
}
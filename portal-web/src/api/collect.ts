// 收藏功能 API
import request from '@/utils/request'

export interface CollectItem {
    id: number
    skuId: number
    skuName: string
    thumbImg: string
    salePrice: number
    marketPrice: number
    stockNum: number
}

// 收藏商品
export function collect(skuId: number) {
    return request({
        url: `/product/auth/collect/${skuId}`,
        method: 'post'
    })
}

// 取消收藏
export function cancelCollect(skuId: number) {
    return request({
        url: `/product/auth/collect/${skuId}`,
        method: 'delete'
    })
}

// 获取收藏列表
export async function getCollectList(): Promise<CollectItem[]> {
    const res = await request({
        url: '/product/auth/collect/list',
        method: 'get'
    })
    // 拦截器返回的是 { code, message, data }，需要取出 data 字段
    return res.data || []
}

// 检查是否已收藏（需要调用后端接口或根据收藏列表判断）
export async function isCollected(skuId: number): Promise<boolean> {
    try {
        const list = await getCollectList()
        return list.some((item: CollectItem) => item.skuId === skuId)
    } catch (error) {
        console.error('检查收藏状态失败:', error)
        return false
    }
}

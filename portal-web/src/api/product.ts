import request from '@/utils/request'

/**
 * 获取首页数据（包含畅销商品）
 * @returns Promise<any>
 */
export function getIndexData() {
    return request({
        url: '/product/index', // 实际后端接口
        method: 'get'
    })
}

/**
 * 获取畅销商品列表
 * @param params 请求参数 { page?: number, pageSize?: number }
 * @returns Promise<any>
 */
export function getBestsellerList(params?: { page?: number, pageSize?: number }) {
    return request({
        url: '/product/index', // 使用首页接口获取畅销商品
        method: 'get',
        params
    })
}

/**
 * 获取猜你喜欢商品列表
 * @param page 页码（从 1 开始）
 * @param limit 每页数量
 * @returns Promise<any>
 */
export function getGuessLikeList(page: number = 1, limit: number = 10) {
    return request({
        url: `/product/recommend/auth/guessYouLike/${page}/${limit}`,
        method: 'get'
    })
}

/**
 * 记录浏览历史
 * @param skuId 商品SKU ID
 * @returns Promise<any>
 */
export function recordBrowse(skuId: number) {
    return request({
        url: `/product/recommend/auth/recordBrowse/${skuId}`,
        method: 'post'
    })
}

/**
 * 获取历史浏览记录
 * @param limit 获取数量（默认5）
 * @returns Promise<any>
 */
export function getBrowseHistory(limit: number = 5) {
    return request({
        url: `/product/recommend/auth/browseHistory/${limit}`,
        method: 'get'
    })
}

/**
 * 搜索商品（使用智能搜索接口）
 * @param params 请求参数 { keyword?: string, page?: number, limit?: number, brandId?: number, category1Id?: number, category2Id?: number, category3Id?: number, order?: number }
 * @returns Promise<any>
 */
export function searchProduct(params?: { 
  keyword?: string
  page?: number
  limit?: number
  brandId?: number
  category1Id?: number
  category2Id?: number
  category3Id?: number
  order?: number 
}) {
    const page = params?.page || 1;
    const limit = params?.limit || 20;
    return request({
        url: `/product/search/${page}/${limit}`,
        method: 'get',
        params: {
            keyword: params?.keyword,
            brandId: params?.brandId,
            category1Id: params?.category1Id,
            category2Id: params?.category2Id,
            category3Id: params?.category3Id,
            order: params?.order
        }
    })
}

/**
 * 获取商品详情
 * @param skuId 商品SKU ID
 * @returns Promise<any>
 */
export function getProductDetail(skuId: string | number) {
    return request({
        url: `/product/item/${skuId}`,
        method: 'get'
    })
}

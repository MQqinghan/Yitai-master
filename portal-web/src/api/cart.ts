import request from '@/utils/request'

// 1. 添加购物车
export function addToCart(skuId, skuNum) {
    return request({
        url: `/order/cart/auth/addToCart/${skuId}/${skuNum}`,
        method: 'get'
    })
}

// 2. 查询购物车列表
export function getCartList() {
    return request({
        url: '/order/cart/auth/cartList',
        method: 'get'
    })
}

// 3. 单个商品选中/取消
export function checkCart(skuId, isChecked) {
    return request({
        url: `/order/cart/auth/checkCart/${skuId}/${isChecked}`,
        method: 'get'
    })
}

// 4. 全选/取消全选
export function allCheckCart(isChecked) {
    return request({
        url: `/order/cart/auth/allCheckCart/${isChecked}`,
        method: 'get'
    })
}

// 5. 删除购物车商品
export function deleteCart(skuId) {
    return request({
        url: `/order/cart/auth/deleteCart/${skuId}`,
        method: 'delete'
    })
}

// 6. 清空购物车
export function clearCart() {
    return request({
        url: '/order/cart/auth/clearCart',
        method: 'get'
    })
}

// 7. 获取所有选中的购物车（结算用）
export function getAllChecked() {
    return request({
        url: '/order/cart/auth/getAllCkecked',
        method: 'get'
    })
}

// 8. 删除已选中的购物车（下单后）
export function deleteChecked() {
    return request({
        url: '/order/cart/auth/deleteChecked',
        method: 'get'
    })
}
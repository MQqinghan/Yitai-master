import request from '@/utils/request'

// 1. 结算页数据（购物车结算）
export function getTrade() {
    return request({
        url: '/order/orderInfo/auth/trade',
        method: 'get'
    })
}

// 2. 立即购买结算（单个商品）
export function buyTrade(skuId) {
    return request({
        url: `/order/orderInfo/auth/buy/${skuId}`,
        method: 'get'
    })
}

// 3. 提交订单
export function submitOrder(data) {
    return request({
        url: '/order/orderInfo/auth/submitOrder',
        method: 'post',
        data
    })
}

// 4. 获取订单详情
export function getOrderInfo(orderId) {
    return request({
        url: `/order/orderInfo/auth/${orderId}`,
        method: 'get'
    })
}

// 5. 获取订单分页列表
export function getOrderList(page, limit, orderStatus = '') {
    return request({
        url: `/order/orderInfo/auth/${page}/${limit}`,
        method: 'get',
        params: { orderStatus }
    })
}

// 6. 更新订单状态（支付成功回调）
export function updateOrderStatus(orderNo) {
    return request({
        url: `/order/orderInfo/auth/updateOrderStatusPayed/${orderNo}`,
        method: 'get'
    })
}

// 7. 支付宝支付（获取支付宝HTML表单）
export function submitAlipay(orderNo: string) {
    return request({
        url: `/order/alipay/submitAlipay/${orderNo}`,
        method: 'get'
    })
}

// 8. 查询订单支付状态
export function getPayStatus(orderNo: string) {
    return request({
        url: `/order/orderInfo/auth/getPayStatus/${orderNo}`,
        method: 'get'
    })
}

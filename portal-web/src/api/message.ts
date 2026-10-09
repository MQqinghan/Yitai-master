import request from '@/utils/request'

/**
 * 获取消息列表
 * @param params 请求参数 { page?: number, pageSize?: number, type?: string }
 * @returns Promise<any>
 */
export function getMessageList(params?: { page?: number, pageSize?: number, type?: string }) {
    return request({
        url: '/user/message/auth/getMessageList',
        method: 'get',
        params
    })
}

/**
 * 获取未读消息数量
 * @returns Promise<any>
 */
export function getUnreadCount() {
    return request({
        url: '/user/message/auth/getUnreadCount',
        method: 'get'
    })
}

/**
 * 标记消息为已读
 * @param messageId 消息ID
 * @returns Promise<any>
 */
export function markAsRead(messageId: number) {
    return request({
        url: `/user/message/auth/markAsRead/${messageId}`,
        method: 'put'
    })
}

/**
 * 全部标记为已读
 * @returns Promise<any>
 */
export function markAllAsRead() {
    return request({
        url: '/user/message/auth/markAllAsRead',
        method: 'put'
    })
}

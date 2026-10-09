import request from '@/utils/request'

// 获取用户地址列表
export function getAddressList() {
    return request({
        url: '/user/userAddress/auth/findUserAddressList',
        method: 'get'
    })
}

// 根据ID获取地址详情
export function getAddressById(id) {
    return request({
        url: `/user/userAddress/getUserAddress/${id}`,
        method: 'get'
    })
}

// 保存地址（新增）
export function saveAddress(data) {
    return request({
        url: '/user/userAddress/auth/saveUserAddress',
        method: 'post',
        data
    })
}

// 更新地址
export function updateAddress(data) {
    return request({
        url: '/user/userAddress/auth/updateUserAddress',
        method: 'put',
        data
    })
}

// 删除地址
export function deleteAddress(id) {
    return request({
        url: `/user/userAddress/auth/deleteUserAddress/${id}`,
        method: 'delete'
    })
}

// 设为默认地址（后端暂无此接口，暂时使用更新接口实现）
export function setDefaultAddress(id) {
    return request({
        url: '/user/userAddress/auth/updateUserAddress',
        method: 'put',
        data: { id, isDefault: 1 }
    })
}

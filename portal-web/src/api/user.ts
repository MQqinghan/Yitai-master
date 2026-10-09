import request from '@/utils/request'

// 登录
export function login(data: { username: string; password: string }) {
    return request({
        url: '/user/userInfo/login',
        method: 'post',
        data
    })
}

// 注册
export function register(data: { username: string; password: string; nickName: string; code: string }) {
    return request({
        url: '/user/userInfo/register',
        method: 'post',
        data
    })
}

// 发送验证码
export function sendSmsCode(phone: string) {
    return request({
        url: `/user/sms/sendCode/${phone}`,
        method: 'get'
    })
}

// 获取当前登录用户信息
export function getUserInfo() {
    return request({
        url: '/user/userInfo/auth/getCurrentUserInfo',
        method: 'get'
    })
}

// 退出登录
export function logout() {
    return request({
        url: '/user/userInfo/auth/logout',
        method: 'post'
    })
}
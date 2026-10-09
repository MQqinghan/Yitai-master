import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import home from '@/views/home.vue'
import searchResult from '@/views/searchResult.vue';
import SearchBar from "@/components/searchBar.vue";
const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'home',
        component: home,
        alias: '/home' // 可选：添加别名，访问/home也跳首页
    },
    {
        path: '/goods/:id',
        name: 'goodsDetail',
        component: () => import('@/views/GoodsDetail.vue')
    },
    {
        path: '/cart',
        name: 'cart',
        component: () => import('@/views/Cart.vue')
    }
    ,
    {
        path: '/search',
        name: 'searchResult',
        component: searchResult,
        props: ( route)=> ({
            keyword: route.query.keyword
        })
    },
    {
        path: '/category',
        name: 'category',
        component: () => import('@/views/category.vue')
    },
    {
        path: '/detail/:skuId',
        name: 'detail',
        component: () => import('@/views/detail.vue')
    },
    {
        path: '/address',
        name: 'Address',
        component: () => import('@/views/address.vue'),
        meta: { title: '收货地址' }
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/register.vue')
    },
    {
        path: '/user',
        name: 'User',
        component: () => import('@/views/user.vue'),
        meta: { title: '个人中心' }
    },
    {
        path: '/setting',
        name: 'Setting',
        component: () => import('@/components/user/setting.vue'),
        meta: { title: '设置' }
    },
    {
        path: '/collection',
        name: 'Collection',
        component: () => import('@/components/user/collection.vue'),
        meta: { title: '我的收藏' }
    },
    {
        path: '/history',
        name: 'History',
        component: () => import('@/views/history.vue'),
        meta: { title: '历史浏览' }
    },
    {
        path: '/message',
        name: 'Message',
        component: () => import('@/views/message.vue'),
        meta: { title: '我的消息' }
    },
    {
        path: '/order/confirm',
        name: 'OrderConfirm',
        component: () => import('@/components/order/orderConfirmation.vue'),
        meta: { title: '确认订单' }
    },
    {
        path: '/order/list',
        name: 'OrderList',
        component: () => import('@/views/orderList.vue'),
        meta: { title: '我的订单' }
    },
    {
        path: '/order/detail',
        name: 'OrderDetail',
        component: () => import('@/components/order/orderDetail.vue'),
        meta: { title: '订单详情' }
    },
    {
        path: '/pay',
        name: 'Pay',
        component: () => import('@/views/pay.vue'),
        meta: { title: '支付' }
    },
    {
        path: '/pay/success',
        name: 'PaySuccess',
        component: () => import('@/views/paySuccess.vue'),
        meta: { title: '支付成功' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
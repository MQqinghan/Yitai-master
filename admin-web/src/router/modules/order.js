const Layout = () => import('@/layout/index.vue')
const orderStatistics = () => import('@/views/order/orderStatistics.vue')
const orderAnalysis = () => import('@/views/order/orderAnalysis.vue')

export default [
    {
      path: '/order',
      component: Layout,
      name: 'order',
      meta: {
        title: '订单管理',
      },
      icon: 'Operation',
      children: [
            {
            path: 'orderStatistics',
            name: 'orderStatistics',
            component: orderStatistics,
            meta: {
                title: '订单统计',
            },
            },
            {
            path: 'orderAnalysis',
            name: 'orderAnalysis',
            component: orderAnalysis,
            meta: {
                title: '数据分析',
            },
            }
        ],
    },
  ]
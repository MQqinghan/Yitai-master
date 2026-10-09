import request from '@/utils/request'

const api_name = '/admin/order/orderInfo'

// 订单统计
export const GetOrderStatisticsData = searchObj => {
  return request({
    url: `${api_name}/getOrderStatisticsData`,
    method: 'get',
    params: searchObj,
  })
}

// 热销商品排行榜
export const GetHotProductList = params => {
  return request({
    url: `${api_name}/getHotProductList`,
    method: 'get',
    params,
  })
}

// 品类销售分析
export const GetCategorySalesList = params => {
  return request({
    url: `${api_name}/getCategorySalesList`,
    method: 'get',
    params,
  })
}

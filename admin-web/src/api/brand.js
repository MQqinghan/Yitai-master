import request from '@/utils/request'

const api_name = '/admin/product/brand'

// 分页列表
export const GetBrandPageList = (page, limit) => {
  return request({
    url: `${api_name}/${page}/${limit}`,
    method: 'get'
  })
}

// 保存品牌
export const SaveBrand = brand => {
  return request({
    url: `${api_name}/save`,
    method: 'post',
    data: brand,
  })
}

// 查询所有的品牌数据
export const FindAllBrand = () => {
  return request({
    url: `${api_name}/findAll`,
    method: 'get',
  })
}

// ========== 新增：修改品牌接口 ==========
export const UpdateBrand = brand => {
  return request({
    url: `${api_name}/updateById`,
    method: 'put', // 对应后端PUT请求
    data: brand,   // 传入包含id的品牌对象
  })
}

// ========== 新增：删除品牌接口 ==========
export const DeleteBrand = id => {
  return request({
    url: `${api_name}/deleteById/${id}`, // 路径传id
    method: 'delete', // 对应后端DELETE请求
  })
}
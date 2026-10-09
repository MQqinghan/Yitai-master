package com.yitai.product.service;

import com.yitai.model.dto.h5.ProductSkuDto;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.vo.h5.ProductItemVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ProductService {

    //2 根据销量排序，获取前10条记录
    List<ProductSku> selectProductSkuBySale();

    //分页查询
    PageInfo<ProductSku> findByPage(Integer page, Integer limit, ProductSkuDto productSkuDto);

    //商品详情接口
    ProductItemVo item(Long skuId);

    //远程调用：根据skuId返回sku信息
    ProductSku getBySkuId(Long skuId);

    //远程调用：更新SKU销量
    void updateSaleNum(Long skuId, Integer num);

    //远程调用：扣减库存
    boolean deductStock(Long skuId, Integer num);
}

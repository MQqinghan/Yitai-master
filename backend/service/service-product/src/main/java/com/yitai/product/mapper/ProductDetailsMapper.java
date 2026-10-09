package com.yitai.product.mapper;

import com.yitai.model.entity.product.ProductDetails;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductDetailsMapper {

    //4 productId，获取商品详情信息
    ProductDetails getByProductId(Long productId);
}

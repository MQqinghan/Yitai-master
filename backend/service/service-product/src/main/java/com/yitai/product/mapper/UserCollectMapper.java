package com.yitai.product.mapper;

import com.yitai.model.entity.product.UserCollect;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserCollectMapper {

    // 收藏商品
    void save(UserCollect userCollect);

    // 取消收藏
    void deleteByUserIdAndSkuId(Long userId, Long skuId);

    // 获取用户收藏列表
    List<UserCollect> getListByUserId(Long userId);

    // 判断是否已收藏
    UserCollect getByUserIdAndSkuId(Long userId, Long skuId);
    
    // 获取用户收藏的SKU ID列表
    List<Long> selectSkuIdsByUserId(Long userId);
    
    // 获取用户最近一次收藏的商品SKU ID
    Long selectLatestCollectSkuId(Long userId);
}

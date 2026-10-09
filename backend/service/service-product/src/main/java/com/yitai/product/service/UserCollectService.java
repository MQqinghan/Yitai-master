package com.yitai.product.service;

import com.yitai.model.vo.product.UserCollectVo;

import java.util.List;

public interface UserCollectService {

    // 收藏商品
    void collect(Long userId, Long skuId);

    // 取消收藏
    void cancelCollect(Long userId, Long skuId);

    // 获取收藏列表
    List<UserCollectVo> getCollectList(Long userId);
}

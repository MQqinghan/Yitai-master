package com.yitai.product.service;

import com.yitai.model.entity.product.ProductSku;

import java.util.List;

public interface RecommendService {

    /**
     * 猜你喜欢推荐算法
     * @param userId 用户ID
     * @param page 页码
     * @param limit 每页数量
     * @return 推荐商品列表
     */
    List<ProductSku> guessYouLike(Long userId, Integer page, Integer limit);

    /**
     * 记录用户浏览历史
     * @param userId 用户ID
     * @param skuId 商品SKU ID
     */
    void recordBrowseHistory(Long userId, Long skuId);

    /**
     * 记录用户搜索历史
     * @param userId 用户ID
     * @param keyword 搜索关键词
     */
    void recordSearchHistory(Long userId, String keyword);

    /**
     * 获取用户最近浏览的商品列表
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 商品列表
     */
    List<ProductSku> getBrowseHistory(Long userId, Integer limit);
}

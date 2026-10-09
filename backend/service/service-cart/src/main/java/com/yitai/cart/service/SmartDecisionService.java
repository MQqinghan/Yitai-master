package com.yitai.cart.service;

public interface SmartDecisionService {

    // 检查购物车中商品的价格变化
    void checkPriceChanges();

    // 检查购物车中商品的库存变化
    void checkStockChanges();

    // 执行智能决策检查（价格和库存）
    void executeSmartDecisionCheck();

}

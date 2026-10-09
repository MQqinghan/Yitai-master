package com.yitai.order.service;

import com.yitai.model.dto.h5.OrderInfoDto;
import com.yitai.model.entity.order.OrderInfo;
import com.yitai.model.vo.h5.TradeVo;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface OrderInfoService {

    //结算
    TradeVo getTrade();

    //生成订单
    OrderInfo submitOrder(OrderInfoDto orderInfoDto);

    //获取订单信息
    OrderInfo getOrderInfo(Long orderId);

    //立即购买
    TradeVo buy(Long skuId);

    //获取订单分页列表
    PageInfo<OrderInfo> findOrderPage(Integer page, Integer limit, Integer orderStatus);

    //远程调用：根据订单编号获取订单信息
    OrderInfo getOrderInfoByOrderNo(String orderNo);

    //更新订单状态
    void updateOrderStatus(String orderNo);
    
    //远程调用：获取用户已购买的SKU ID列表(用于推荐系统)
    List<Long> getPurchasedSkuIds(Long userId);
    
    //远程调用：获取用户购买历史类目统计(用于推荐系统)
    List<Map<String, Object>> getPurchaseCategoryStats(Long userId);
}

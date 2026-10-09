package com.yitai.order.mapper;

import com.yitai.model.entity.order.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper {

    //添加数据到order_item表
    void save(OrderItem orderItem);

    //根据订单id查询订单项
    List<OrderItem> findByOrderId(Long orderId);
    
    //远程调用：获取用户已购买的SKU ID列表
    List<Long> selectPurchasedSkuIdsByUserId(@Param("userId") Long userId);
    
    //远程调用：获取用户购买历史类目统计
    List<Map<String, Object>> selectPurchaseCategoryStats(@Param("userId") Long userId);
}

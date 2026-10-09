package com.yitai.feign.order;

import com.yitai.model.entity.order.OrderInfo;
import com.yitai.model.vo.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("service-order")
public interface OrderFeignClient {

    @GetMapping("/api/order/orderInfo/auth/getOrderInfoByOrderNo/{orderNo}")
    public OrderInfo getOrderInfoByOrderNo(@PathVariable("orderNo") String orderNo);

    //更新订单状态
    @GetMapping("/api/order/orderInfo/updateOrderStatusPayed/{orderNo}")
    public Result updateOrderStatus(@PathVariable(value = "orderNo") String orderNo);
    
    //远程调用：获取用户已购买的SKU ID列表(用于推荐系统排除已购买商品)
    @GetMapping("/api/order/orderInfo/auth/getPurchasedSkuIds/{userId}")
    public List<Long> getPurchasedSkuIds(@PathVariable("userId") Long userId);
    
    //远程调用：获取用户购买历史类目统计(用于推荐系统计算购买偏好)
    @GetMapping("/api/order/orderInfo/auth/getPurchaseCategoryStats/{userId}")
    public List<java.util.Map<String, Object>> getPurchaseCategoryStats(@PathVariable("userId") Long userId);
}

package com.yitai.order.controller;

import com.yitai.model.dto.h5.OrderInfoDto;
import com.yitai.model.entity.order.OrderInfo;
import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.model.vo.h5.TradeVo;
import com.yitai.order.service.OrderInfoService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import java.util.List;
import java.util.Map;

@Tag(name = "订单管理")
@RestController
@RequestMapping(value="/api/order/orderInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    //更新订单状态
    @GetMapping("updateOrderStatusPayed/{orderNo}")
    public Result updateOrderStatus(@PathVariable(value = "orderNo") String orderNo) {
        orderInfoService.updateOrderStatus(orderNo);
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }

    //查询订单支付状态（供前端轮询使用）
    @Operation(summary = "查询订单支付状态")
    @GetMapping("auth/getPayStatus/{orderNo}")
    public Result getPayStatus(@PathVariable String orderNo) {
        OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        return Result.build(orderInfo, ResultCodeEnum.SUCCESS);
    }

    //生成订单
    @Operation(summary = "提交订单")
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfoDto orderInfoDto) {
        OrderInfo orderInfo = orderInfoService.submitOrder(orderInfoDto);
        return Result.build(orderInfo,ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "确认下单")
    @GetMapping("auth/trade")
    public Result<TradeVo> trade() {
        TradeVo tradeVo = orderInfoService.getTrade();
        return Result.build(tradeVo, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "获取订单信息")
    @GetMapping("auth/{orderId}")
    public Result getOrderInfo(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        return Result.build(orderInfo,ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "立即购买")
    @GetMapping("auth/buy/{skuId}")
    public Result buy(@PathVariable Long skuId) {
        TradeVo tradeVo = orderInfoService.buy(skuId);
        return Result.build(tradeVo,ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "获取订单分页列表")
    @GetMapping("auth/{page}/{limit}")
    public Result<PageInfo<OrderInfo>> list(@PathVariable Integer page,
                                            @PathVariable Integer limit,
                                            @RequestParam(required = false, defaultValue = "") Integer orderStatus) {
        PageInfo<OrderInfo> pageInfo =
                orderInfoService.findOrderPage(page,limit,orderStatus);
        return Result.build(pageInfo,ResultCodeEnum.SUCCESS);
    }

    //远程调用：根据订单编号获取订单信息
    @Operation(summary = "获取订单信息")
    @GetMapping("auth/getOrderInfoByOrderNo/{orderNo}")
    @ResponseBody
    public OrderInfo getOrderInfoByOrderNo(@Parameter(name = "orderNo", description = "订单编号", required = true) @PathVariable String orderNo) {
       return orderInfoService.getOrderInfoByOrderNo(orderNo);
    }
    
    //远程调用：获取用户已购买的SKU ID列表(用于推荐系统)
    @Operation(summary = "获取用户已购买SKU列表")
    @GetMapping("auth/getPurchasedSkuIds/{userId}")
    @ResponseBody
    public List<Long> getPurchasedSkuIds(@PathVariable Long userId) {
        return orderInfoService.getPurchasedSkuIds(userId);
    }
    
    //远程调用：获取用户购买历史类目统计(用于推荐系统)
    @Operation(summary = "获取用户购买类目统计")
    @GetMapping("auth/getPurchaseCategoryStats/{userId}")
    @ResponseBody
    public List<java.util.Map<String, Object>> getPurchaseCategoryStats(@PathVariable Long userId) {
        return orderInfoService.getPurchaseCategoryStats(userId);
    }
}

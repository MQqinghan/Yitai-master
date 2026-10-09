package com.yitai.manager.controller;

import com.yitai.manager.service.OrderInfoService;
import com.yitai.model.dto.order.HotProductDto;
import com.yitai.model.dto.order.OrderStatisticsDto;
import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.model.vo.order.CategorySalesVo;
import com.yitai.model.vo.order.HotProductVo;
import com.yitai.model.vo.order.OrderStatisticsVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "订单管理")
@RestController
@RequestMapping(value="/admin/order/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService ;

    @Operation(summary = "获取订单统计数据")
    @GetMapping("/getOrderStatisticsData")
    public Result getOrderStatisticsData(OrderStatisticsDto orderStatisticsDto) {
        OrderStatisticsVo orderStatisticsVo =
                orderInfoService.getOrderStatisticsData(orderStatisticsDto);
        return Result.build(orderStatisticsVo, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "获取热销商品排行榜")
    @GetMapping("/getHotProductList")
    public Result getHotProductList(HotProductDto hotProductDto) {
        List<HotProductVo> hotProductList = orderInfoService.getHotProductList(hotProductDto);
        return Result.build(hotProductList, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "获取品类销售统计")
    @GetMapping("/getCategorySalesList")
    public Result getCategorySalesList(HotProductDto hotProductDto) {
        List<CategorySalesVo> categorySalesList = orderInfoService.getCategorySalesList(hotProductDto);
        return Result.build(categorySalesList, ResultCodeEnum.SUCCESS);
    }
}

package com.yitai.manager.service;

import com.yitai.model.dto.order.HotProductDto;
import com.yitai.model.dto.order.OrderStatisticsDto;
import com.yitai.model.vo.order.CategorySalesVo;
import com.yitai.model.vo.order.HotProductVo;
import com.yitai.model.vo.order.OrderStatisticsVo;

import java.util.List;

public interface OrderInfoService {

    OrderStatisticsVo getOrderStatisticsData(OrderStatisticsDto orderStatisticsDto);

    //查询热销商品排行榜
    List<HotProductVo> getHotProductList(HotProductDto hotProductDto);

    //查询品类销售统计
    List<CategorySalesVo> getCategorySalesList(HotProductDto hotProductDto);
}

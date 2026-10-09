package com.yitai.manager.mapper;

import com.yitai.model.dto.order.HotProductDto;
import com.yitai.model.entity.order.OrderStatistics;
import com.yitai.model.vo.order.CategorySalesVo;
import com.yitai.model.vo.order.HotProductVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderInfoMapper {
    //统计前一天交易金额
    OrderStatistics selectStatisticsByDate(String createDate);

    //实时查询日期范围内的订单统计数据（按天分组）
    List<OrderStatistics> selectStatisticsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    //查询热销商品排行榜
    List<HotProductVo> selectHotProductList(@Param("dto") HotProductDto dto);

    //查询品类销售统计
    List<CategorySalesVo> selectCategorySalesList(@Param("dto") HotProductDto dto);
}

package com.yitai.manager.service.impl;

import cn.hutool.core.date.DateUtil;
import com.yitai.manager.mapper.OrderInfoMapper;
import com.yitai.manager.mapper.OrderStatisticsMapper;
import com.yitai.manager.service.OrderInfoService;
import com.yitai.model.dto.order.HotProductDto;
import com.yitai.model.dto.order.OrderStatisticsDto;
import com.yitai.model.entity.order.OrderStatistics;
import com.yitai.model.vo.order.CategorySalesVo;
import com.yitai.model.vo.order.HotProductVo;
import com.yitai.model.vo.order.OrderStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {

    @Autowired
    private OrderStatisticsMapper orderStatisticsMapper ;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Override
    public OrderStatisticsVo getOrderStatisticsData(OrderStatisticsDto orderStatisticsDto) {

        //实时查询order_info表，根据日期范围统计订单数据
        List<OrderStatistics> orderStatisticsList =
                orderInfoMapper.selectStatisticsByDateRange(
                        orderStatisticsDto.getCreateTimeBegin(),
                        orderStatisticsDto.getCreateTimeEnd());

        //遍历list集合，得到所有日期，把所有日期封装list新集合里面
        List<String> dateList = orderStatisticsList.stream()
                .map(orderStatistics ->
                        DateUtil.format(orderStatistics.getOrderDate(), "yyyy-MM-dd"))
                .collect(Collectors.toList());

        //遍历list集合，得到所有日期对应总金额，把总金额封装list新集合里面
        List<BigDecimal> decimalList = orderStatisticsList.stream()
                .map(OrderStatistics::getTotalAmount)
                .collect(Collectors.toList());

        //把两个list集合封装OrderStatisticsVo，返回OrderStatisticsVo（返回数据）
        OrderStatisticsVo orderStatisticsVo = new OrderStatisticsVo();
        orderStatisticsVo.setDateList(dateList);
        orderStatisticsVo.setAmountList(decimalList);
        return orderStatisticsVo;
    }

    @Override
    public List<HotProductVo> getHotProductList(HotProductDto hotProductDto) {
        // 查询热销商品列表
        List<HotProductVo> hotProductList = orderInfoMapper.selectHotProductList(hotProductDto);
        
        // 设置排名
        for (int i = 0; i < hotProductList.size(); i++) {
            hotProductList.get(i).setRank(i + 1);
        }
        
        return hotProductList;
    }

    @Override
    public List<CategorySalesVo> getCategorySalesList(HotProductDto hotProductDto) {
        // 查询品类销售统计
        List<CategorySalesVo> categorySalesList = orderInfoMapper.selectCategorySalesList(hotProductDto);
        
        // 计算总销售额
        BigDecimal totalAmount = categorySalesList.stream()
                .map(CategorySalesVo::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算每个品类的销售占比
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            categorySalesList.forEach(category -> {
                if (category.getTotalAmount() != null) {
                    double ratio = category.getTotalAmount()
                            .divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
                            .doubleValue() * 100;
                    category.setSalesRatio(Math.round(ratio * 100.0) / 100.0); // 保留两位小数
                } else {
                    category.setSalesRatio(0.0);
                }
            });
        }
        
        return categorySalesList;
    }
}

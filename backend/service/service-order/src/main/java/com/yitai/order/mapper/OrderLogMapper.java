package com.yitai.order.mapper;

import com.yitai.model.entity.order.OrderLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderLogMapper {

    //6 添加数据到order_log表
    void save(OrderLog orderLog);
}

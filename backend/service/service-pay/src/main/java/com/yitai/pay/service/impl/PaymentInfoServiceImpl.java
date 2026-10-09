package com.yitai.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.yitai.feign.order.OrderFeignClient;
import com.yitai.feign.product.ProductFeignClient;
import com.yitai.model.entity.order.OrderInfo;
import com.yitai.model.entity.order.OrderItem;
import com.yitai.model.entity.pay.PaymentInfo;
import com.yitai.pay.mapper.PaymentInfoMapper;
import com.yitai.pay.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    //保存支付记录
    @Override
    public PaymentInfo savePaymentInfo(String orderNo) {
        //1 根据订单编号查询支付记录
        PaymentInfo paymentInfo = paymentInfoMapper.getByOrderNo(orderNo);

        //2 判断支付记录是否存在
        if(paymentInfo == null) {
            //远程调用获取订单信息
            OrderInfo orderInfo = orderFeignClient.getOrderInfoByOrderNo(orderNo);
            
            // === 调试日志开始 ===
            System.out.println("=== Feign 返回的 OrderInfo 对象 ===");
            System.out.println("订单号: " + (orderInfo != null ? orderInfo.getOrderNo() : "null"));
            System.out.println("商品列表是否为空: " + (orderInfo == null || orderInfo.getOrderItemList() == null));
            if (orderInfo != null && orderInfo.getOrderItemList() != null) {
                System.out.println("商品数量: " + orderInfo.getOrderItemList().size());
                for (int i = 0; i < orderInfo.getOrderItemList().size(); i++) {
                    OrderItem item = orderInfo.getOrderItemList().get(i);
                    System.out.println("  商品" + (i+1) + ": " + item.getSkuName() + ", 价格: " + item.getSkuPrice() + ", 数量: " + item.getSkuNum());
                }
            } else {
                System.out.println("⚠️ 警告: orderInfo 或 orderItemList 为 null!");
            }
            System.out.println("=================================");
            // === 调试日志结束 ===
            
            //把orderInfo获取数据封装到paymentInfo里面
            paymentInfo = new PaymentInfo();
            paymentInfo.setUserId(orderInfo.getUserId());
            paymentInfo.setPayType(orderInfo.getPayType());
            String content = "";
            for(OrderItem item : orderInfo.getOrderItemList()) {
                content += item.getSkuName() + " ";
            }
            paymentInfo.setContent(content);
            paymentInfo.setAmount(orderInfo.getTotalAmount());
            paymentInfo.setOrderNo(orderNo);
            paymentInfo.setPaymentStatus(0);
            //添加
            paymentInfoMapper.save(paymentInfo);
        }
        return paymentInfo;
    }

    //支付完成，更新状态
    @Override
    public void updatePaymentStatus(Map<String, String> map) {
       // map.get("out_trade_no")
        //1 根据订单编号查询支付记录信息
        PaymentInfo paymentInfo = paymentInfoMapper.getByOrderNo(map.get("out_trade_no"));

        //2 判断支付记录是否存在
        if(paymentInfo == null) {
            return;
        }

        //3 判断如果支付记录已经完成，不需要更新
        if(paymentInfo.getPaymentStatus()==1) {
            return;
        }

        //3 没有完成才更新
        paymentInfo.setPaymentStatus(1);
        paymentInfo.setOutTradeNo(map.get("trade_no"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(JSON.toJSONString(map));
        paymentInfoMapper.updatePaymentInfo(paymentInfo);

        //更新订单状态
        orderFeignClient.updateOrderStatus(paymentInfo.getOrderNo());

        //更新sku商品销量和扣减库存
        // 1. 远程调用获取订单信息，包含订单明细
        OrderInfo orderInfo = orderFeignClient.getOrderInfoByOrderNo(paymentInfo.getOrderNo());
        // 2. 遍历订单明细，更新每个SKU的销量和扣减库存
        if (orderInfo != null && orderInfo.getOrderItemList() != null) {
            for (OrderItem orderItem : orderInfo.getOrderItemList()) {
                // 调用商品服务，更新对应SKU的销量
                productFeignClient.updateSaleNum(orderItem.getSkuId(), orderItem.getSkuNum());
                
                // 调用商品服务，扣减对应SKU的库存
                boolean success = productFeignClient.deductStock(orderItem.getSkuId(), orderItem.getSkuNum());
                if (!success) {
                    // 库存扣减失败，记录日志（理论上不应该发生，因为下单时已经校验过库存）
                    System.err.println("库存扣减失败: skuId=" + orderItem.getSkuId() + ", num=" + orderItem.getSkuNum());
                }
            }
        }
    }
}

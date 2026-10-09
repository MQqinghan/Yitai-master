package com.yitai.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.yitai.common.exception.YitaiException;
import com.yitai.model.entity.pay.PaymentInfo;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.pay.service.AlipayService;
import com.yitai.pay.service.PaymentInfoService;
import com.yitai.pay.utils.AlipayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;

@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private AlipayProperties alipayProperties;

    @Autowired
    private AlipayClient alipayClient;

    //支付宝下单
    @Override
    public String submitAlipay(String orderNo) {

        //1 保存支付记录
        PaymentInfo paymentInfo = paymentInfoService.savePaymentInfo(orderNo);

        //2 调用支付宝服务接口
        //构建需要参数，进行调用
        AlipayTradePagePayRequest alipayRequest  = new AlipayTradePagePayRequest();
        // 同步回调
        alipayRequest.setReturnUrl(alipayProperties.getReturnPaymentUrl());
        // 异步回调
        alipayRequest.setNotifyUrl(alipayProperties.getNotifyPaymentUrl());

        // 准备请求参数 ,声明一个map 集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOrderNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getAmount());
        map.put("subject",paymentInfo.getContent());
        alipayRequest.setBizContent(JSON.toJSONString(map));

        //调用支付宝服务接口
        try {
            System.out.println("========== 开始调用支付宝接口 ==========");
            System.out.println("订单号: " + paymentInfo.getOrderNo());
            System.out.println("支付金额: " + paymentInfo.getAmount());
            System.out.println("商品描述: " + paymentInfo.getContent());
            System.out.println("请求参数: " + JSON.toJSONString(map));
            
            AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);
            
            System.out.println("支付宝响应码: " + response.getCode());
            System.out.println("支付宝响应信息: " + response.getMsg());
            System.out.println("支付宝子码: " + response.getSubCode());
            System.out.println("支付宝子信息: " + response.getSubMsg());
            System.out.println("是否成功: " + response.isSuccess());
            
            if(response.isSuccess()) {
                String form = response.getBody();
                System.out.println("支付宝表单长度: " + (form != null ? form.length() : 0));
                System.out.println("支付宝表单内容(前200字符): " + (form != null ? form.substring(0, Math.min(200, form.length())) : "null"));
                System.out.println("========== 支付宝调用成功 ==========");
                return form;
            } else {
                System.out.println("========== 支付宝调用失败 ==========");
                System.err.println("错误码: " + response.getCode());
                System.err.println("错误信息: " + response.getMsg());
                System.err.println("子错误码: " + response.getSubCode());
                System.err.println("子错误信息: " + response.getSubMsg());
                throw new YitaiException(ResultCodeEnum.DATA_ERROR);
            }
        } catch (AlipayApiException e) {
            System.err.println("========== 支付宝API异常 ==========");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

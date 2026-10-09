package com.yitai.order.service.impl;

import com.yitai.common.exception.YitaiException;
import com.yitai.feign.cart.CartFeignClient;
import com.yitai.feign.product.ProductFeignClient;
import com.yitai.feign.user.UserFeignClient;
import com.yitai.model.dto.h5.OrderInfoDto;
import com.yitai.model.entity.h5.CartInfo;
import com.yitai.model.entity.order.OrderInfo;
import com.yitai.model.entity.order.OrderItem;
import com.yitai.model.entity.order.OrderLog;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.entity.user.UserAddress;
import com.yitai.model.entity.user.UserInfo;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.model.vo.h5.TradeVo;
import com.yitai.order.mapper.OrderInfoMapper;
import com.yitai.order.mapper.OrderItemMapper;
import com.yitai.order.mapper.OrderLogMapper;
import com.yitai.order.service.OrderInfoService;
import com.yitai.utils.AuthContextUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    //结算
    @Override
    public TradeVo getTrade() {

        //远程调用获取购物车选中商品列表
        List<CartInfo> cartInfoList = cartFeignClient.getAllCkecked();
        //创建list集合用于封装订单项
        List<OrderItem> orderItemList = new ArrayList<>();
        for(CartInfo cartInfo : cartInfoList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setThumbImg(cartInfo.getImgUrl());
            orderItemList.add(orderItem);
        }

        //获取订单支付总金额
        BigDecimal totalAmount = new BigDecimal(0);
        for(OrderItem orderItem:orderItemList) {
            totalAmount = totalAmount.add(orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum())));
        }

        TradeVo tradeVo = new TradeVo();
        tradeVo.setOrderItemList(orderItemList);
        tradeVo.setTotalAmount(totalAmount);
        return tradeVo;
    }

    //生成订单
    @Override
    public OrderInfo submitOrder(OrderInfoDto orderInfoDto) {
        //1 orderInfoDto获取所有订单项list List<OrderItem>
        List<OrderItem> orderItemList = orderInfoDto.getOrderItemList();

        //2 判断List<OrderItem>为空，抛出异常
        if(CollectionUtils.isEmpty(orderItemList)) {
            throw new YitaiException(ResultCodeEnum.DATA_ERROR);
        }

        //3 校验商品库存是否充足
        // 遍历List<OrderItem>集合，得到每个orderItem
        for(OrderItem orderItem : orderItemList) {
            //根据skuId获取sku信息，远程调用获取商品sku信息，（包含库存量）
            ProductSku productSku = productFeignClient.getBySkuId(orderItem.getSkuId());
            if(productSku == null) {
                throw new YitaiException(ResultCodeEnum.DATA_ERROR);
            }
            // 校验每个orderItem库存量是否充足
            if(orderItem.getSkuNum().intValue() > productSku.getStockNum().intValue()) {
                throw new YitaiException(ResultCodeEnum.STOCK_LESS);
            }
            
            // 补充：防止前端传参缺失，从商品服务兜底获取图片和名称
            if (orderItem.getThumbImg() == null || orderItem.getThumbImg().isEmpty()) {
                orderItem.setThumbImg(productSku.getThumbImg());
            }
            if (orderItem.getSkuName() == null || orderItem.getSkuName().isEmpty()) {
                orderItem.setSkuName(productSku.getSkuName());
            }
            if (orderItem.getSkuPrice() == null) {
                orderItem.setSkuPrice(productSku.getSalePrice());
            }
        }

        //4 添加数据到order_info表
        // 封装数据到OrderInfo对象
        OrderInfo orderInfo = new OrderInfo();
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        //订单编号
        orderInfo.setOrderNo(String.valueOf(System.currentTimeMillis()));
        //用户id
        orderInfo.setUserId(userInfo.getId());
        //用户昵称
        orderInfo.setNickName(userInfo.getNickName());

        //封装收货地址信息
        Long userAddressId = orderInfoDto.getUserAddressId();
        //远程调用：根据收货地址id，获取用户收货地址信息
        UserAddress userAddress = userFeignClient.getUserAddress(userAddressId);
        orderInfo.setReceiverName(userAddress.getName());
        orderInfo.setReceiverPhone(userAddress.getPhone());
        orderInfo.setReceiverTagName(userAddress.getTagName());
        orderInfo.setReceiverProvince(userAddress.getProvinceCode());
        orderInfo.setReceiverCity(userAddress.getCityCode());
        orderInfo.setReceiverDistrict(userAddress.getDistrictCode());
        orderInfo.setReceiverAddress(userAddress.getFullAddress());
        //订单金额
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderItem orderItem : orderItemList) {
            totalAmount = totalAmount.add(orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum())));
        }
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setCouponAmount(new BigDecimal(0));
        orderInfo.setOriginalTotalAmount(totalAmount);
        orderInfo.setFeightFee(orderInfoDto.getFeightFee());
        orderInfo.setPayType(2);
        orderInfo.setOrderStatus(0);

        orderInfoMapper.save(orderInfo);

        //5 添加数据到order_item表
        //添加List<OrderItem>里面数据，把集合每个orderItem添加表
        for(OrderItem orderItem:orderItemList) {
            //设置对应订单id
            orderItem.setOrderId(orderInfo.getId());
            orderItemMapper.save(orderItem);
        }

        //6 添加数据到order_log表
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderInfo.getId());
        orderLog.setProcessStatus(0);
        orderLog.setNote("提交订单");
        orderLogMapper.save(orderLog);

        //7 远程调用：把生成订单商品，从购物车删除
        cartFeignClient.deleteChecked();

        //8 返回完整订单信息（包含orderId和orderNo）
        return orderInfo;
    }

    //获取订单信息
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.getById(orderId);
        if (orderInfo != null) {
            // 查询订单商品列表
            List<OrderItem> orderItemList = orderItemMapper.findByOrderId(orderId);
            orderInfo.setOrderItemList(orderItemList);
        }
        return orderInfo;
    }

    //立即购买
    @Override
    public TradeVo buy(Long skuId) {
        //封装订单项集合
        List<OrderItem> orderItemList = new ArrayList<>();
        ProductSku productSku = productFeignClient.getBySkuId(skuId);
        OrderItem orderItem = new OrderItem();
        orderItem.setSkuId(skuId);
        orderItem.setSkuName(productSku.getSkuName());
        orderItem.setSkuNum(1);
        orderItem.setSkuPrice(productSku.getSalePrice());
        orderItem.setThumbImg(productSku.getThumbImg());
        orderItemList.add(orderItem);

        TradeVo tradeVo = new TradeVo();
        tradeVo.setOrderItemList(orderItemList);
        tradeVo.setTotalAmount(productSku.getSalePrice());
        return tradeVo;
    }

    //获取订单分页列表
    @Override
    public PageInfo<OrderInfo> findOrderPage(Integer page, Integer limit, Integer orderStatus) {
        PageHelper.startPage(page,limit);
        //查询订单信息
        Long userId = AuthContextUtil.getUserInfo().getId();
        List<OrderInfo> orderInfoList = orderInfoMapper.findUserPage(userId,orderStatus);

        //订单里面所有订单项
        orderInfoList.forEach(orderInfo -> {
            //订单id查询订单里面订单项
            List<OrderItem> orderItemList = orderItemMapper.findByOrderId(orderInfo.getId());
            //封装
            orderInfo.setOrderItemList(orderItemList);
        });

        return new PageInfo<>(orderInfoList);
    }

    //远程调用：根据订单编号获取订单信息
    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.getOrderInfoByOrderNo(orderNo);
        if (orderInfo != null) {
            // 查询并设置订单项列表
            List<OrderItem> orderItemList = orderItemMapper.findByOrderId(orderInfo.getId());
            orderInfo.setOrderItemList(orderItemList);
        }
        return orderInfo;
    }

    //更新订单状态
    @Override
    public void updateOrderStatus(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.getOrderInfoByOrderNo(orderNo);
        
        if (orderInfo != null) {
            orderInfo.setOrderStatus(1);
            orderInfo.setPaymentTime(new Date());
            orderInfo.setPayType(2);
            
            orderInfoMapper.updateById(orderInfo);
        }
    }
    
    //远程调用：获取用户已购买的SKU ID列表(用于推荐系统)
    @Override
    public List<Long> getPurchasedSkuIds(Long userId) {
        return orderItemMapper.selectPurchasedSkuIdsByUserId(userId);
    }
    
    //远程调用：获取用户购买历史类目统计(用于推荐系统)
    @Override
    public List<Map<String, Object>> getPurchaseCategoryStats(Long userId) {
        return orderItemMapper.selectPurchaseCategoryStats(userId);
    }
}

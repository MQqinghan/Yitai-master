package com.yitai.feign.cart;

import com.yitai.model.entity.h5.CartInfo;
import com.yitai.model.vo.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value = "service-cart")
public interface CartFeignClient {

    @GetMapping("/api/order/cart/auth/deleteChecked")
    public Result deleteChecked();

    @GetMapping(value = "/api/order/cart/auth/getAllCkecked")
    public List<CartInfo> getAllCkecked();
    
    //远程调用：获取用户购物车所有商品列表
    @GetMapping(value = "/api/order/cart/auth/cartList")
    public List<CartInfo> getCartList();
}

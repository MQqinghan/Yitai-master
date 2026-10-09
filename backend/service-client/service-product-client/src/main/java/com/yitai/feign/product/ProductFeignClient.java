package com.yitai.feign.product;

import com.yitai.model.entity.product.ProductSku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-product")
public interface ProductFeignClient {

    @GetMapping("/api/product/getBySkuId/{skuId}")
    public ProductSku getBySkuId(@PathVariable("skuId") Long skuId);

    //更新SKU销量
    @GetMapping("/api/product/updateSaleNum/{skuId}/{num}")
    public void updateSaleNum(@PathVariable("skuId") Long skuId, @PathVariable("num") Integer num);

    //扣减库存
    @GetMapping("/api/product/deductStock/{skuId}/{num}")
    public boolean deductStock(@PathVariable("skuId") Long skuId, @PathVariable("num") Integer num);

}

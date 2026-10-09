package com.yitai.product.controller;

import com.yitai.model.dto.h5.ProductSkuDto;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.entity.user.UserInfo;
import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.model.vo.h5.ProductItemVo;
import com.yitai.product.service.ProductService;
import com.yitai.product.service.RecommendService;
import com.yitai.product.service.SearchService;
import com.yitai.model.vo.h5.SearchResultVo;
import com.yitai.utils.AuthContextUtil;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private SearchService searchService;

    //商品详情接口
    @Operation(summary = "商品详情")
    @GetMapping("item/{skuId}")
    public Result item(@PathVariable Long skuId) {
        ProductItemVo productItemVo = productService.item(skuId);
        
        // 记录浏览历史
        try {
            UserInfo userInfo = AuthContextUtil.getUserInfo();
            if (userInfo != null) {
                recommendService.recordBrowseHistory(userInfo.getId(), skuId);
            }
        } catch (Exception e) {
            // 记录浏览历史失败不影响正常商品详情展示
            System.err.println("记录浏览历史异常: " + e.getMessage());
        }
        
        return Result.build(productItemVo,ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "分页查询")
    @GetMapping(value = "/{page}/{limit}")
    public Result list(@PathVariable Integer page,
                       @PathVariable Integer limit,
                       ProductSkuDto productSkuDto,
                       HttpServletRequest httpRequest) {
       System.out.println("========== ProductController.list 被调用 ==========");
       System.out.println("page=" + page + ", limit=" + limit);
       System.out.println("productSkuDto=" + (productSkuDto == null ? "null" : productSkuDto.toString()));
       if (productSkuDto != null) {
           System.out.println("keyword=" + productSkuDto.getKeyword());
       }
          
       // 调试：打印所有请求头
       System.out.println("========== 请求头信息 ==========");
       java.util.Enumeration<String> headerNames = httpRequest.getHeaderNames();
       while (headerNames.hasMoreElements()) {
           String headerName = headerNames.nextElement();
           System.out.println(headerName + " = " + httpRequest.getHeader(headerName));
       }
       System.out.println("=================================");
          
       // 记录搜索历史（如果有搜索关键词）
       if (productSkuDto != null && productSkuDto.getKeyword() != null && !productSkuDto.getKeyword().trim().isEmpty()) {
           try {
               UserInfo userInfo = AuthContextUtil.getUserInfo();
               if (userInfo != null) {
                   recommendService.recordSearchHistory(userInfo.getId(), productSkuDto.getKeyword());
               }
           } catch (Exception e) {
               // 记录搜索历史失败不影响正常搜索，只记录日志
           }
       }
          
       PageInfo<ProductSku> pageInfo = productService.findByPage(page,limit,productSkuDto);
       return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    //远程调用：根据skuId返回sku信息
    @GetMapping("/getBySkuId/{skuId}")
    public ProductSku getBySkuId(@PathVariable Long skuId) {
        ProductSku productSku = productService.getBySkuId(skuId);
        return productSku;
    }

    //远程调用：更新SKU销量
    @GetMapping("/updateSaleNum/{skuId}/{num}")
    public void updateSaleNum(@PathVariable Long skuId, @PathVariable Integer num) {
        productService.updateSaleNum(skuId, num);
    }

    //远程调用：扣减库存
    @GetMapping("/deductStock/{skuId}/{num}")
    public boolean deductStock(@PathVariable Long skuId, @PathVariable Integer num) {
        return productService.deductStock(skuId, num);
    }

    @Operation(summary = "智能搜索（Elasticsearch）")
    @GetMapping(value = "/search/{page}/{limit}")
    public Result<SearchResultVo> search(@PathVariable Integer page,
                                         @PathVariable Integer limit,
                                         ProductSkuDto productSkuDto) {
        // 记录搜索历史
        if (productSkuDto != null && productSkuDto.getKeyword() != null && !productSkuDto.getKeyword().trim().isEmpty()) {
            try {
                UserInfo userInfo = AuthContextUtil.getUserInfo();
                if (userInfo != null) {
                    recommendService.recordSearchHistory(userInfo.getId(), productSkuDto.getKeyword());
                }
            } catch (Exception e) {
                // 记录搜索历史失败不影响正常搜索
            }
        }
        
        SearchResultVo result = searchService.search(productSkuDto, page, limit);
        return Result.build(result, ResultCodeEnum.SUCCESS);
    }
}

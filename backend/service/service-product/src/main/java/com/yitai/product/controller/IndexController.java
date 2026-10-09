package com.yitai.product.controller;

import com.yitai.model.entity.product.Category;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.model.vo.h5.IndexVo;
import com.yitai.product.service.CategoryService;
import com.yitai.product.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "首页接口管理")
@RestController
@RequestMapping(value="/api/product/index")
//@CrossOrigin //跨域
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result index() {
        //1 所有一级分类
        List<Category> categoryList = categoryService.selectOneCategory();

        //2 根据销量排序，获取前10条记录
        List<ProductSku> productSkuList = productService.selectProductSkuBySale();

        //3 封装数据到vo对象里面
        IndexVo indexVo = new IndexVo();
        indexVo.setCategoryList(categoryList);
        indexVo.setProductSkuList(productSkuList);
        return Result.build(indexVo, ResultCodeEnum.SUCCESS);
    }
}

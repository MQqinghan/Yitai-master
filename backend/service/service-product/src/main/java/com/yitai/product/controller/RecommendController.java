package com.yitai.product.controller;

import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.entity.user.UserInfo;
import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.product.service.RecommendService;
import com.yitai.utils.AuthContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品推荐管理")
@RestController
@RequestMapping(value = "/api/product/recommend")
@SuppressWarnings({"unchecked", "rawtypes"})
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Operation(summary = "猜你喜欢")
    @GetMapping("auth/guessYouLike/{page}/{limit}")
    public Result<List<ProductSku>> guessYouLike(
            @PathVariable Integer page,
            @PathVariable Integer limit) {
        // 从线程上下文中获取当前登录用户信息
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        if (userInfo == null) {
            return Result.build(null, ResultCodeEnum.LOGIN_AUTH);
        }
        Long userId = userInfo.getId();
        List<ProductSku> list = recommendService.guessYouLike(userId, page, limit);
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "记录浏览历史")
    @PostMapping("auth/recordBrowse/{skuId}")
    public Result recordBrowse(@PathVariable Long skuId) {
        // 从线程上下文中获取当前登录用户信息
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        if (userInfo == null) {
            return Result.build(null, ResultCodeEnum.LOGIN_AUTH);
        }
        Long userId = userInfo.getId();
        recommendService.recordBrowseHistory(userId, skuId);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "获取浏览历史")
    @GetMapping("auth/browseHistory/{limit}")
    public Result<List<ProductSku>> getBrowseHistory(@PathVariable Integer limit) {
        // 从线程上下文中获取当前登录用户信息
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        if (userInfo == null) {
            return Result.build(null, ResultCodeEnum.LOGIN_AUTH);
        }
        Long userId = userInfo.getId();
        List<ProductSku> list = recommendService.getBrowseHistory(userId, limit);
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }
}

package com.yitai.product.controller;

import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.model.vo.product.UserCollectVo;
import com.yitai.product.service.UserCollectService;
import com.yitai.utils.AuthContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户收藏管理")
@RestController
@RequestMapping(value = "/api/product/auth/collect")
public class UserCollectController {

    @Autowired
    private UserCollectService userCollectService;

    @Operation(summary = "收藏商品")
    @PostMapping("/{skuId}")
    public Result collect(@PathVariable Long skuId) {
        // 从ThreadLocal获取当前登录用户信息
        Long userId = AuthContextUtil.getUserInfo().getId();
        userCollectService.collect(userId, skuId);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{skuId}")
    public Result cancelCollect(@PathVariable Long skuId) {
        // 从ThreadLocal获取当前登录用户信息
        Long userId = AuthContextUtil.getUserInfo().getId();
        userCollectService.cancelCollect(userId, skuId);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "获取收藏列表")
    @GetMapping("/list")
    public Result getCollectList() {
        // 从ThreadLocal获取当前登录用户信息
        Long userId = AuthContextUtil.getUserInfo().getId();
        List<UserCollectVo> list = userCollectService.getCollectList(userId);
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }
}

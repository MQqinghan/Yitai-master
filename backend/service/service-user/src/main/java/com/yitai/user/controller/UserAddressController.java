package com.yitai.user.controller;

import com.yitai.model.entity.user.UserAddress;
import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.user.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户地址接口")
@RestController
@RequestMapping(value="/api/user/userAddress")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @Operation(summary = "获取用户地址列表")
    @GetMapping("auth/findUserAddressList")
    public Result findUserAddressList() {
        List<UserAddress> list = userAddressService.findUserAddressList();
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }

    //根据id获取收货地址信息
    @GetMapping("getUserAddress/{id}")
    public UserAddress getUserAddress(@PathVariable Long id) {
        return userAddressService.getUserAddress(id);
    }

    @Operation(summary = "保存用户地址")
    @PostMapping("auth/saveUserAddress")
    public Result saveUserAddress(@RequestBody UserAddress userAddress) {
        userAddressService.save(userAddress);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "更新用户地址")
    @PutMapping("auth/updateUserAddress")
    public Result updateUserAddress(@RequestBody UserAddress userAddress) {
        userAddressService.update(userAddress);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "删除用户地址")
    @DeleteMapping("auth/deleteUserAddress/{id}")
    public Result deleteUserAddress(@PathVariable Long id) {
        userAddressService.delete(id);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }
}

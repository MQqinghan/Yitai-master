package com.yitai.user.service;

import com.yitai.model.entity.user.UserAddress;

import java.util.List;

public interface UserAddressService {

    //获取用户地址列表
    List<UserAddress> findUserAddressList();

    //根据id获取收货地址信息
    UserAddress getUserAddress(Long id);

    //保存用户地址
    void save(UserAddress userAddress);

    //更新用户地址
    void update(UserAddress userAddress);

    //删除用户地址
    void delete(Long id);
}

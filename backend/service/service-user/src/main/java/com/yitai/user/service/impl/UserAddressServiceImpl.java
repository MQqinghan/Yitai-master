package com.yitai.user.service.impl;

import com.yitai.model.entity.user.UserAddress;
import com.yitai.user.mapper.UserAddressMapper;
import com.yitai.user.service.UserAddressService;
import com.yitai.utils.AuthContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    //获取用户地址列表
    @Override
    public List<UserAddress> findUserAddressList() {
        Long userId = AuthContextUtil.getUserInfo().getId();
        return userAddressMapper.findUserAddressList(userId);
    }

    //根据id获取收货地址信息
    @Override
    public UserAddress getUserAddress(Long id) {
        return userAddressMapper.getUserAddress(id);
    }

    //保存用户地址
    @Override
    public void save(UserAddress userAddress) {
        Long userId = AuthContextUtil.getUserInfo().getId();
        userAddress.setUserId(userId);
        userAddressMapper.save(userAddress);
    }

    //更新用户地址
    @Override
    public void update(UserAddress userAddress) {
        userAddressMapper.update(userAddress);
    }

    //删除用户地址
    @Override
    public void delete(Long id) {
        userAddressMapper.delete(id);
    }
}

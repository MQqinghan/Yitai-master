package com.yitai.user.mapper;

import com.yitai.model.entity.user.UserAddress;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserAddressMapper {

    //获取用户地址列表
    List<UserAddress> findUserAddressList(Long userId);

    UserAddress getUserAddress(Long id);

    //保存用户地址
    void save(UserAddress userAddress);

    //更新用户地址
    void update(UserAddress userAddress);

    //删除用户地址
    void delete(Long id);
}

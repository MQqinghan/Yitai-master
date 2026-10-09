package com.yitai.feign.user;

import com.yitai.model.entity.user.MessageInfo;
import com.yitai.model.entity.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("service-user")
public interface UserFeignClient {

    @GetMapping("/api/user/userAddress/getUserAddress/{id}")
    public UserAddress getUserAddress(@PathVariable("id") Long id);

    // 保存消息（供其他服务调用）
    @PostMapping("/api/user/message/save")
    public void saveMessage(@RequestBody MessageInfo messageInfo);

    // 保存消息（带去重检查，供其他服务调用）
    @PostMapping("/api/user/message/saveWithDedup")
    public Boolean saveMessageWithDedup(@RequestBody MessageInfo messageInfo);
}

package com.yitai.user.controller;

import com.yitai.model.entity.user.MessageInfo;
import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.user.service.MessageInfoService;
import com.yitai.utils.AuthContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/user/message")
public class MessageInfoController {

    @Autowired
    private MessageInfoService messageInfoService;

    @Operation(summary = "获取当前用户的消息列表")
    @GetMapping("auth/getMessageList")
    public Result<List<MessageInfo>> getMessageList() {
        Long userId = AuthContextUtil.getUserInfo().getId();
        List<MessageInfo> messageList = messageInfoService.getMessageList(userId);
        return Result.build(messageList, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("auth/getUnreadCount")
    public Result<Map<String, Integer>> getUnreadCount() {
        Long userId = AuthContextUtil.getUserInfo().getId();
        int unreadCount = messageInfoService.getUnreadCount(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("unreadCount", unreadCount);
        return Result.build(result, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("auth/markAsRead/{messageId}")
    public Result markAsRead(@PathVariable Long messageId) {
        Long userId = AuthContextUtil.getUserInfo().getId();
        messageInfoService.markAsRead(messageId, userId);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("auth/markAllAsRead")
    public Result markAllAsRead() {
        Long userId = AuthContextUtil.getUserInfo().getId();
        messageInfoService.markAllAsRead(userId);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("auth/delete/{messageId}")
    public Result deleteMessage(@PathVariable Long messageId) {
        Long userId = AuthContextUtil.getUserInfo().getId();
        messageInfoService.deleteMessage(messageId, userId);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "保存消息（内部接口，供其他服务调用）")
    @PostMapping("save")
    public Result saveMessage(@RequestBody MessageInfo messageInfo) {
        messageInfoService.saveMessage(messageInfo);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "保存消息（带去重检查，内部接口）")
    @PostMapping("saveWithDedup")
    public Result<Boolean> saveMessageWithDedup(@RequestBody MessageInfo messageInfo) {
        boolean success = messageInfoService.saveMessageWithDedup(messageInfo);
        return Result.build(success, ResultCodeEnum.SUCCESS);
    }

}

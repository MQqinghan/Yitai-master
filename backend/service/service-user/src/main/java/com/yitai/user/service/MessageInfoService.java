package com.yitai.user.service;

import com.yitai.model.entity.user.MessageInfo;

import java.util.List;

public interface MessageInfoService {

    // 保存消息
    void saveMessage(MessageInfo messageInfo);

    // 保存消息（带去重检查）
    boolean saveMessageWithDedup(MessageInfo messageInfo);

    // 查询用户的消息列表
    List<MessageInfo> getMessageList(Long userId);

    // 查询用户的未读消息数量
    int getUnreadCount(Long userId);

    // 标记消息为已读
    void markAsRead(Long messageId, Long userId);

    // 全部标记为已读
    void markAllAsRead(Long userId);

    // 删除消息
    void deleteMessage(Long messageId, Long userId);

}

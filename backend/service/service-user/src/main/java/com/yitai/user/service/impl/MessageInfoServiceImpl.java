package com.yitai.user.service.impl;

import com.yitai.model.entity.user.MessageInfo;
import com.yitai.user.mapper.MessageInfoMapper;
import com.yitai.user.service.MessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MessageInfoServiceImpl implements MessageInfoService {

    @Autowired
    private MessageInfoMapper messageInfoMapper;

    // 去重时间窗口（小时）
    private static final int DEDUP_HOURS = 8;

    @Override
    public void saveMessage(MessageInfo messageInfo) {
        // 默认未读
        if (messageInfo.getIsRead() == null) {
            messageInfo.setIsRead(0);
        }
        messageInfoMapper.save(messageInfo);
    }

    @Override
    public boolean saveMessageWithDedup(MessageInfo messageInfo) {
        // 1. 检查是否存在去重标识
        if (messageInfo.getDedupKey() == null || messageInfo.getDedupKey().trim().isEmpty()) {
            // 没有去重标识，直接保存
            saveMessage(messageInfo);
            return true;
        }

        // 2. 检查去重时间窗口内是否已存在相同的未读消息
        int count = messageInfoMapper.countByDedupKeyAndTime(
            messageInfo.getDedupKey(), 
            messageInfo.getUserId(), 
            DEDUP_HOURS
        );

        if (count > 0) {
            log.info("消息去重：用户{}的去重标识【{}】在{}小时内已存在{}条未读消息，跳过创建",
                messageInfo.getUserId(), 
                messageInfo.getDedupKey(), 
                DEDUP_HOURS,
                count);
            return false;
        }

        // 3. 不存在重复消息，创建新消息
        saveMessage(messageInfo);
        log.info("创建消息成功：用户{}，去重标识【{}】", messageInfo.getUserId(), messageInfo.getDedupKey());
        return true;
    }

    @Override
    public List<MessageInfo> getMessageList(Long userId) {
        return messageInfoMapper.selectByUserId(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return messageInfoMapper.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        messageInfoMapper.updateReadStatus(messageId, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        messageInfoMapper.updateAllRead(userId);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        messageInfoMapper.deleteById(messageId, userId);
    }

}

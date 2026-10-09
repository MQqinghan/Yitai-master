package com.yitai.user.mapper;

import com.yitai.model.entity.user.MessageInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageInfoMapper {

    // 保存消息
    void save(MessageInfo messageInfo);

    // 查询用户的消息列表
    List<MessageInfo> selectByUserId(@Param("userId") Long userId);

    // 查询用户的未读消息数量
    int countUnreadByUserId(@Param("userId") Long userId);

    // 标记消息为已读
    void updateReadStatus(@Param("messageId") Long messageId, @Param("userId") Long userId);

    // 全部标记为已读
    void updateAllRead(@Param("userId") Long userId);

    // 删除消息
    void deleteById(@Param("messageId") Long messageId, @Param("userId") Long userId);

    // 检查去重标识在指定时间内是否存在未读消息
    int countByDedupKeyAndTime(@Param("dedupKey") String dedupKey, 
                               @Param("userId") Long userId, 
                               @Param("hours") int hours);

}

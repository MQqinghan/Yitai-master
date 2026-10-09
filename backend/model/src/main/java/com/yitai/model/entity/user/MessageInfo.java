package com.yitai.model.entity.user;

import com.yitai.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "消息实体类")
public class MessageInfo extends BaseEntity {

   private static final long serialVersionUID = 1L;

   @Schema(description = "用户id")
   private Long userId;

   @Schema(description = "消息标题")
   private String title;

   @Schema(description = "消息内容")
   private String content;

   @Schema(description = "消息类型：1-系统通知 2-订单通知 3-促销活动 4-降价提醒 5-库存预警")
   private Integer messageType;

   @Schema(description = "是否已读：0-未读 1-已读")
   private Integer isRead;

   @Schema(description = "关联业务id（如订单id、商品id等）")
   private Long businessId;

   @Schema(description = "消息去重标识（如：price_drop_123 表示商品123的降价提醒）")
   private String dedupKey;

   @Schema(description = "消息级别：1-普通 2-重要")
   private Integer messageLevel;

}

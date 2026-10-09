-- 消息提醒表
CREATE TABLE `message_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `message_type` tinyint(4) NOT NULL COMMENT '消息类型：1-系统通知 2-订单通知 3-促销活动 4-降价提醒 5-库存预警',
  `is_read` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读 1-已读',
  `business_id` bigint(20) DEFAULT NULL COMMENT '关联业务ID（如订单ID、商品ID等）',
  `dedup_key` varchar(100) DEFAULT NULL COMMENT '消息去重标识（如：price_drop_123 表示商品123的降价提醒）',
  `message_level` tinyint(4) DEFAULT '1' COMMENT '消息级别：1-普通 2-重要 3-紧急',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_message_type` (`message_type`),
  KEY `idx_dedup_key` (`dedup_key`),
  UNIQUE KEY `uk_dedup_key_user` (`dedup_key`, `user_id`, `is_deleted`) COMMENT '去重唯一约束（8小时内未读消息）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息提醒表';

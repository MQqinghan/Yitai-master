-- ============================================================
-- Yitai 商城 全量建库脚本（库名 yitai_mall，2026-09-28 反向生成）
-- 由 gen_schema.py 从实体类生成，风格对齐 message_info.sql
-- 执行顺序：本文件 -> generate_test_data.sql -> add_analysis_menu_complete.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `yitai_mall` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `yitai_mall`;

-- ========== ProductUnit -> product_unit ==========
DROP TABLE IF EXISTS `product_unit`;
CREATE TABLE `product_unit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product_unit';

-- ========== Region -> region ==========
DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(255) DEFAULT NULL COMMENT '区域编码',
  `parent_code` bigint(20) DEFAULT NULL COMMENT '父区域编码',
  `name` varchar(255) DEFAULT NULL COMMENT '父区名称',
  `level` int(11) DEFAULT NULL COMMENT '地区级别：1-省、自治区、直辖市 2-地级市、地区、自治州、盟 3-市辖区、县级市、县',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='region';

-- ========== CartInfo -> cart_info ==========
DROP TABLE IF EXISTS `cart_info`;
CREATE TABLE `cart_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `sku_id` bigint(20) DEFAULT NULL COMMENT 'skuid',
  `cart_price` decimal(10,2) DEFAULT NULL COMMENT '放入购物车时价格',
  `sku_num` int(11) DEFAULT NULL COMMENT '数量',
  `img_url` varchar(255) DEFAULT NULL COMMENT '图片文件',
  `sku_name` varchar(255) DEFAULT NULL COMMENT 'sku名称 (冗余)',
  `is_checked` int(11) DEFAULT NULL COMMENT 'isChecked',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_sku_id` (`sku_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='cart_info';

-- ========== OrderInfo -> order_info ==========
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '会员_id',
  `nick_name` varchar(100) DEFAULT NULL COMMENT '昵称',
  `order_no` varchar(64) DEFAULT NULL COMMENT '订单号',
  `coupon_id` bigint(20) DEFAULT NULL COMMENT '使用的优惠券',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '订单总额',
  `coupon_amount` decimal(10,2) DEFAULT NULL COMMENT '优惠券',
  `original_total_amount` decimal(10,2) DEFAULT NULL COMMENT '原价金额',
  `feight_fee` decimal(10,2) DEFAULT NULL COMMENT '运费',
  `pay_type` int(11) DEFAULT NULL COMMENT '支付方式【1->微信】',
  `order_status` int(11) DEFAULT NULL COMMENT '订单状态【0->待付款；1->待发货；2->已发货；3->待用户收货，已完成；-1->已取消】',
  `receiver_name` varchar(255) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人电话',
  `receiver_tag_name` varchar(50) DEFAULT NULL COMMENT '收货人地址标签',
  `receiver_province` varchar(255) DEFAULT NULL COMMENT '省份/直辖市',
  `receiver_city` varchar(255) DEFAULT NULL COMMENT '城市',
  `receiver_district` varchar(255) DEFAULT NULL COMMENT '区',
  `receiver_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '确认收货时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消订单时间',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '取消订单原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order_info';

-- ========== OrderItem -> order_item ==========
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `sku_id` bigint(20) DEFAULT NULL COMMENT '商品sku编号',
  `sku_name` varchar(255) DEFAULT NULL COMMENT '商品sku名字',
  `thumb_img` varchar(255) DEFAULT NULL COMMENT '商品sku图片',
  `sku_price` decimal(10,2) DEFAULT NULL COMMENT '商品sku价格',
  `sku_num` int(11) DEFAULT NULL COMMENT '商品购买的数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_order_id` (`order_id`),
  KEY `idx_sku_id` (`sku_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order_item';

-- ========== OrderLog -> order_log ==========
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `operate_user` varchar(255) DEFAULT NULL COMMENT '操作人：用户；系统；后台管理员',
  `process_status` int(11) DEFAULT NULL COMMENT '订单状态',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_order_id` (`order_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order_log';

-- ========== OrderStatistics -> order_statistics ==========
DROP TABLE IF EXISTS `order_statistics`;
CREATE TABLE `order_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_date` datetime DEFAULT NULL COMMENT 'orderDate',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT 'totalAmount',
  `total_num` int(11) DEFAULT NULL COMMENT 'totalNum',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order_statistics';

-- ========== PaymentInfo -> payment_info ==========
DROP TABLE IF EXISTS `payment_info`;
CREATE TABLE `payment_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `order_no` varchar(64) DEFAULT NULL COMMENT '订单号',
  `pay_type` int(11) DEFAULT NULL COMMENT '付款方式：1-微信 2-支付宝',
  `out_trade_no` varchar(255) DEFAULT NULL COMMENT '交易编号（微信或支付）',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `content` text DEFAULT NULL COMMENT '交易内容',
  `payment_status` int(11) DEFAULT NULL COMMENT '支付状态：0-未支付 1-已支付',
  `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
  `callback_content` varchar(255) DEFAULT NULL COMMENT '回调信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_user_id` (`user_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='payment_info';

-- ========== Brand -> brand ==========
DROP TABLE IF EXISTS `brand`;
CREATE TABLE `brand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) DEFAULT NULL COMMENT '品牌名称',
  `logo` varchar(255) DEFAULT NULL COMMENT '品牌logo',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='brand';

-- ========== Category -> category ==========
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) DEFAULT NULL COMMENT '分类名称',
  `image_url` varchar(255) DEFAULT NULL COMMENT '分类图片url',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父节点id',
  `status` int(11) DEFAULT NULL COMMENT '分类状态: 是否显示[0-不显示，1显示]',
  `order_num` int(11) DEFAULT NULL COMMENT '排序字段',
  `has_children` varchar(255) DEFAULT NULL COMMENT '是否存在子节点',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_parent_id` (`parent_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='category';

-- ========== CategoryBrand -> category_brand ==========
DROP TABLE IF EXISTS `category_brand`;
CREATE TABLE `category_brand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `brand_id` bigint(20) DEFAULT NULL COMMENT '品牌id',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类id',
  `category_name` varchar(255) DEFAULT NULL COMMENT '分类名称',
  `brand_name` varchar(255) DEFAULT NULL COMMENT '品牌名称',
  `logo` varchar(255) DEFAULT NULL COMMENT '品牌logo',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_category_id` (`category_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='category_brand';

-- ========== Product -> product ==========
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `brand_id` bigint(20) DEFAULT NULL COMMENT '品牌id',
  `category1_id` bigint(20) DEFAULT NULL COMMENT '一级分类id',
  `category2_id` bigint(20) DEFAULT NULL COMMENT '二级分类id',
  `category3_id` bigint(20) DEFAULT NULL COMMENT '三级分类id',
  `unit_name` varchar(255) DEFAULT NULL COMMENT '计量单位',
  `slider_urls` varchar(255) DEFAULT NULL COMMENT '轮播图url',
  `spec_value` varchar(255) DEFAULT NULL COMMENT '商品规格值json串',
  `status` int(11) DEFAULT NULL COMMENT '线上状态：0-初始值，1-上架，-1-自主下架',
  `audit_status` int(11) DEFAULT NULL COMMENT '审核状态',
  `audit_message` varchar(255) DEFAULT NULL COMMENT '审核信息',
  `brand_name` varchar(255) DEFAULT NULL COMMENT '品牌名称',
  `category1_name` varchar(255) DEFAULT NULL COMMENT '一级分类名称',
  `category2_name` varchar(255) DEFAULT NULL COMMENT '二级分类名称',
  `category3_name` varchar(255) DEFAULT NULL COMMENT '三级分类名称',
  `details_image_urls` varchar(255) DEFAULT NULL COMMENT '图片详情列表',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_category1_id` (`category1_id`),
  KEY `idx_category2_id` (`category2_id`),
  KEY `idx_category3_id` (`category3_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product';

-- ========== ProductDetails -> product_details ==========
DROP TABLE IF EXISTS `product_details`;
CREATE TABLE `product_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint(20) DEFAULT NULL COMMENT 'productId',
  `image_urls` varchar(255) DEFAULT NULL COMMENT 'imageUrls',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_product_id` (`product_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product_details';

-- ========== ProductSku -> product_sku ==========
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sku_code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `sku_name` varchar(255) DEFAULT NULL COMMENT 'skuName',
  `product_id` bigint(20) DEFAULT NULL COMMENT '商品ID',
  `thumb_img` varchar(255) DEFAULT NULL COMMENT '缩略图路径',
  `sale_price` decimal(10,2) DEFAULT NULL COMMENT '售价',
  `market_price` decimal(10,2) DEFAULT NULL COMMENT '市场价',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `stock_num` int(11) DEFAULT NULL COMMENT '库存数',
  `sale_num` int(11) DEFAULT NULL COMMENT '销量',
  `sku_spec` varchar(255) DEFAULT NULL COMMENT 'sku规格信息json',
  `weight` varchar(255) DEFAULT NULL COMMENT '重量',
  `volume` varchar(255) DEFAULT NULL COMMENT '体积',
  `status` int(11) DEFAULT NULL COMMENT '线上状态：0-初始值，1-上架，-1-自主下架',
  `product_name` varchar(255) DEFAULT NULL COMMENT '商品名称（关联product表）',
  `brand_name` varchar(255) DEFAULT NULL COMMENT '品牌名称（关联brand表）',
  `category1_id` bigint(20) DEFAULT NULL COMMENT '一级分类ID',
  `category2_id` bigint(20) DEFAULT NULL COMMENT '二级分类ID',
  `category3_id` bigint(20) DEFAULT NULL COMMENT '三级分类ID',
  `brand_id` bigint(20) DEFAULT NULL COMMENT '品牌ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_product_id` (`product_id`),
  KEY `idx_category1_id` (`category1_id`),
  KEY `idx_category2_id` (`category2_id`),
  KEY `idx_category3_id` (`category3_id`),
  KEY `idx_brand_id` (`brand_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product_sku';

-- ========== ProductSpec -> product_spec ==========
DROP TABLE IF EXISTS `product_spec`;
CREATE TABLE `product_spec` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spec_name` varchar(255) DEFAULT NULL COMMENT '规格名称',
  `spec_value` varchar(255) DEFAULT NULL COMMENT '规格值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product_spec';

-- ========== UserBrowseHistory -> user_browse_history ==========
DROP TABLE IF EXISTS `user_browse_history`;
CREATE TABLE `user_browse_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `sku_id` bigint(20) DEFAULT NULL COMMENT '商品skuID',
  `browse_count` int(11) DEFAULT NULL COMMENT '浏览次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_sku_id` (`sku_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user_browse_history';

-- ========== UserCollect -> user_collect ==========
DROP TABLE IF EXISTS `user_collect`;
CREATE TABLE `user_collect` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `sku_id` bigint(20) DEFAULT NULL COMMENT '商品skuID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_sku_id` (`sku_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user_collect';

-- ========== UserSearchHistory -> user_search_history ==========
DROP TABLE IF EXISTS `user_search_history`;
CREATE TABLE `user_search_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `keyword` varchar(255) DEFAULT NULL COMMENT '搜索关键词',
  `search_count` int(11) DEFAULT NULL COMMENT '搜索次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_user_id` (`user_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user_search_history';

-- ========== SysMenu -> sys_menu ==========
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父节点id',
  `title` varchar(200) DEFAULT NULL COMMENT '节点标题',
  `component` varchar(255) DEFAULT NULL COMMENT '组件名称',
  `sort_value` int(11) DEFAULT NULL COMMENT '排序值',
  `status` int(11) DEFAULT NULL COMMENT '状态(0:禁止,1:正常)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_parent_id` (`parent_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sys_menu';

-- ========== SysOperLog -> sys_oper_log ==========
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) DEFAULT NULL COMMENT '模块标题',
  `method` varchar(255) DEFAULT NULL COMMENT '方法名称',
  `request_method` varchar(255) DEFAULT NULL COMMENT '请求方式',
  `business_type` int(11) DEFAULT NULL COMMENT '请求方式',
  `operator_type` varchar(255) DEFAULT NULL COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(255) DEFAULT NULL COMMENT '操作人员',
  `oper_url` varchar(255) DEFAULT NULL COMMENT '请求URL',
  `oper_ip` varchar(255) DEFAULT NULL COMMENT '主机地址',
  `oper_param` varchar(255) DEFAULT NULL COMMENT '请求参数',
  `json_result` varchar(255) DEFAULT NULL COMMENT '返回参数',
  `status` int(11) DEFAULT NULL COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(255) DEFAULT NULL COMMENT '错误消息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sys_oper_log';

-- ========== SysRole -> sys_role ==========
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(255) DEFAULT NULL COMMENT '角色名称',
  `role_code` varchar(255) DEFAULT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sys_role';

-- ========== SysRoleUser -> sys_user_role ==========
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT 'roleId',
  `user_id` bigint(20) DEFAULT NULL COMMENT 'userId',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_role_id` (`role_id`),
  KEY `idx_user_id` (`user_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sys_user_role';

-- ========== SysRoleMenu -> sys_role_menu（无实体类，以 manager 的 SysRoleMenuMapper.xml 列为准） ==========
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色id',
  `menu_id` bigint(20) DEFAULT NULL COMMENT '菜单id',
  `is_half` tinyint(4) NOT NULL DEFAULT '0' COMMENT '半开状态：0-选中 1-半选（父菜单）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sys_role_menu';

-- ========== SysUser -> sys_user ==========
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_name` varchar(255) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `salt` varchar(255) DEFAULT NULL COMMENT '密码盐值（用于盐值加密）',
  `name` varchar(255) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `avatar` varchar(500) DEFAULT NULL COMMENT '图像',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `status` int(11) DEFAULT NULL COMMENT '状态（1：正常 0：停用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sys_user';

-- ========== MessageInfo -> message_info ==========
DROP TABLE IF EXISTS `message_info`;
CREATE TABLE `message_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `title` varchar(200) DEFAULT NULL COMMENT '消息标题',
  `content` text DEFAULT NULL COMMENT '消息内容',
  `message_type` int(11) DEFAULT NULL COMMENT '消息类型：1-系统通知 2-订单通知 3-促销活动 4-降价提醒 5-库存预警',
  `is_read` int(11) DEFAULT NULL COMMENT '是否已读：0-未读 1-已读',
  `business_id` bigint(20) DEFAULT NULL COMMENT '关联业务id（如订单id、商品id等）',
  `dedup_key` varchar(100) DEFAULT NULL COMMENT '消息去重标识（如：price_drop_123 表示商品123的降价提醒）',
  `message_level` int(11) DEFAULT NULL COMMENT '消息级别：1-普通 2-重要',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_business_id` (`business_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='message_info';

-- ========== UserAddress -> user_address ==========
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `name` varchar(255) DEFAULT NULL COMMENT 'name',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `tag_name` varchar(255) DEFAULT NULL COMMENT '标签名称',
  `province_code` varchar(255) DEFAULT NULL COMMENT 'provinceCode',
  `city_code` varchar(255) DEFAULT NULL COMMENT 'cityCode',
  `district_code` varchar(255) DEFAULT NULL COMMENT 'districtCode',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `full_address` varchar(255) DEFAULT NULL COMMENT '完整地址',
  `is_default` int(11) DEFAULT NULL COMMENT '是否默认地址（0：否 1：是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_user_id` (`user_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user_address';

-- ========== UserInfo -> user_info ==========
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `nick_name` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `sex` int(11) DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话号码',
  `memo` varchar(500) DEFAULT NULL COMMENT '备注',
  `open_id` varchar(64) DEFAULT NULL COMMENT '微信open id',
  `union_id` varchar(64) DEFAULT NULL COMMENT '微信开放平台unionID',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后一次登录ip',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后一次登录时间',
  `status` int(11) DEFAULT NULL COMMENT '状态：1为正常，0为禁止',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除',
  KEY `idx_open_id` (`open_id`),
  KEY `idx_union_id` (`union_id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user_info';

SET FOREIGN_KEY_CHECKS = 1;
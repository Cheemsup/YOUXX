-- ============================================
-- YOUXX 数据库建表脚本
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_general_ci
-- ============================================

CREATE DATABASE IF NOT EXISTS `youxx`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `youxx`;

-- ============================================
-- 1. 用户表 sys_user
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` VARCHAR(10) NOT NULL COMMENT '用户ID (A001/U001)',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名，唯一',
  `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
  `status` VARCHAR(10) NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL/DISABLED',
  `avatar` TEXT DEFAULT NULL COMMENT '头像资源路径 (/upload_resources/user_icon/xxx.png)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================
-- 2. 商品分类表 product_category
-- ============================================
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` VARCHAR(20) NOT NULL COMMENT '分类ID (drinks/snacks等)',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标标识',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- ============================================
-- 3. 商品表 product
-- ============================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` VARCHAR(10) NOT NULL COMMENT '商品ID (P001)',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category_id` VARCHAR(20) NOT NULL COMMENT '分类ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `unit` VARCHAR(20) NOT NULL COMMENT '计量单位 (瓶/包/个/盒)',
  `stock` INT NOT NULL DEFAULT 999 COMMENT '库存数量',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '图片路径URL',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
  `bar_code` VARCHAR(50) DEFAULT NULL COMMENT '条码',
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣系数 (0-1, 1表示无折扣)',
  `is_hot` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否热销: 0否/1是',
  `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签,逗号分隔',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ONSHELF' COMMENT '状态: ONSHELF/OFFSHELF',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- ============================================
-- 4. 订单表 orders
-- ============================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` VARCHAR(30) NOT NULL COMMENT '订单号',
  `user_id` VARCHAR(10) NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '下单用户名',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `item_count` INT NOT NULL DEFAULT 0 COMMENT '商品总件数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/SHIPPED/COMPLETED/CANCELLED',
  `urgent_count` INT NOT NULL DEFAULT 0 COMMENT '催单次数',
  `last_urgent_time` DATETIME DEFAULT NULL COMMENT '最后催单时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- ============================================
-- 5. 订单明细表 order_item
-- ============================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `order_id` VARCHAR(30) NOT NULL COMMENT '关联订单号',
  `product_id` VARCHAR(10) NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称(快照)',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价(快照)',
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣(快照)',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `unit` VARCHAR(20) NOT NULL COMMENT '计量单位(快照)',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细表';

-- ============================================
-- 6. 消息表 message
-- ============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` VARCHAR(30) NOT NULL COMMENT '消息ID',
  `conversation_id` VARCHAR(50) NOT NULL COMMENT '会话ID',
  `sender` VARCHAR(20) NOT NULL COMMENT '发送者: USER/ADMIN',
  `sender_name` VARCHAR(50) NOT NULL COMMENT '发送者显示名',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0否/1是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_sender` (`sender`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息表';

-- ============================================
-- 7. 用户地址表 user_address
-- ============================================
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `user_id` VARCHAR(10) NOT NULL COMMENT '所属用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `detail` VARCHAR(500) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认: 0否/1是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户收货地址表';

-- ============================================
-- 8. 系统设置表 sys_settings
-- ============================================
DROP TABLE IF EXISTS `sys_settings`;
CREATE TABLE `sys_settings` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `setting_key` VARCHAR(100) NOT NULL COMMENT '设置键名',
  `setting_value` VARCHAR(500) DEFAULT NULL COMMENT '设置值',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统设置表';

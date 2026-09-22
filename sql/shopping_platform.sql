-- ============================================================
-- 线上购物平台数据库初始化脚本（MySQL 8.x / 5.7 兼容版）
-- 执行方式：mysql -u root -p < shopping_platform.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `shopping_platform`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `shopping_platform`;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名，唯一',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码',
    `nickname`    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER 普通用户 / ADMIN 管理员',
    `avatar`      VARCHAR(255)          DEFAULT NULL COMMENT '头像地址',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `seller_id`   BIGINT         NOT NULL COMMENT '发布人（卖家）用户ID',
    `title`       VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `description` TEXT           NOT NULL COMMENT '商品详情描述',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '售价',
    `cover_image` VARCHAR(255)   NOT NULL COMMENT '商品封面图地址',
    `category_id` BIGINT         NULL COMMENT '商品分类ID，关联 category 表',
    `status`      VARCHAR(20)    NOT NULL DEFAULT 'ONSALE' COMMENT '状态：ONSALE 可售 / RESERVED 已预购 / OUT_OF_STOCK 无库存',
    `listed`      TINYINT(1)     NOT NULL DEFAULT 1 COMMENT '是否上架：1 上架 / 0 下架',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_seller` (`seller_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='商品表';

DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`    VARCHAR(40)    NOT NULL COMMENT '订单编号，唯一',
    `product_id`  BIGINT         NOT NULL COMMENT '商品ID',
    `buyer_id`    BIGINT         NOT NULL COMMENT '买家用户ID',
    `seller_id`   BIGINT         NOT NULL COMMENT '卖家用户ID',
    `amount`      DECIMAL(10, 2) NOT NULL COMMENT '下单金额（商品价格快照）',
    `status`      VARCHAR(20)    NOT NULL DEFAULT 'PAID' COMMENT '订单状态：PAID 已支付',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_order_buyer` (`buyer_id`),
    KEY `idx_order_product` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单表';

-- ============================================================
-- 二期：分类 / 价格 / 订单状态 / 评论 / 修改记录
-- ============================================================

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='商品分类表';

DROP TABLE IF EXISTS `product_price`;
CREATE TABLE `product_price` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '价格记录ID',
    `product_id`  BIGINT         NOT NULL COMMENT '商品ID',
    `price_name`  VARCHAR(50)    NOT NULL DEFAULT '售价' COMMENT '价格名称：售价/原价等',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '价格金额',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_price_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='商品价格表';

DROP TABLE IF EXISTS `order_status`;
CREATE TABLE `order_status` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '状态ID',
    `status_code` VARCHAR(20) NOT NULL COMMENT '状态编码',
    `status_name` VARCHAR(50) NOT NULL COMMENT '状态名称',
    `description` VARCHAR(255)         DEFAULT NULL COMMENT '状态说明',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_status_code` (`status_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='订单状态表';

DROP TABLE IF EXISTS `product_comment`;
CREATE TABLE `product_comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `product_id`  BIGINT       NOT NULL COMMENT '商品ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论用户ID',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_comment_product` (`product_id`),
    KEY `idx_comment_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='商品评论表';

DROP TABLE IF EXISTS `user_change_log`;
CREATE TABLE `user_change_log` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `change_type` VARCHAR(20) NOT NULL COMMENT '修改类型：INFO 个人信息 / PASSWORD 密码',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_change_user` (`user_id`),
    KEY `idx_user_change_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户修改记录表';

-- 初始分类与订单状态数据
INSERT IGNORE INTO `category` (`name`, `sort`) VALUES
('生活用品', 1),
('运动', 2),
('数码科技', 3),
('食品生鲜', 4),
('服饰鞋包', 5),
('美妆个护', 6),
('图书文娱', 7),
('家居家装', 8),
('母婴玩具', 9),
('办公用品', 10);

INSERT IGNORE INTO `order_status` (`status_code`, `status_name`, `description`, `sort`) VALUES
('PAID', '已支付', '订单已支付成功', 1),
('CANCELLED', '已取消', '订单已取消', 2),
('REFUNDING', '退款中', '退款处理中', 3),
('REFUNDED', '已退款', '已退款完成', 4);

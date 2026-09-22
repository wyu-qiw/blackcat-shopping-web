-- ============================================================
-- 线上购物平台数据库初始化脚本
-- 数据库：MySQL 8.x，字符集 utf8mb4
-- 执行方式：mysql -u root -p < shopping_platform.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `shopping_platform`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `shopping_platform`;

-- 用户表：普通用户 USER / 平台管理员 ADMIN
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

-- 商品表：上架/下架由 listed 控制，状态仅三种：可售/已预购/无库存
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `seller_id`   BIGINT         NOT NULL COMMENT '发布人（卖家）用户ID',
    `title`       VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `description` TEXT           NOT NULL COMMENT '商品详情描述',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '售价',
    `cover_image` VARCHAR(255)   NOT NULL COMMENT '商品封面图地址',
    `status`      VARCHAR(20)    NOT NULL DEFAULT 'ONSALE' COMMENT '状态：ONSALE 可售 / RESERVED 已预购 / OUT_OF_STOCK 无库存',
    `listed`      TINYINT(1)     NOT NULL DEFAULT 1 COMMENT '是否上架：1 上架 / 0 下架',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_seller` (`seller_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='商品表';

-- 订单表：购买成功后商品自动变为已预购
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

-- 说明：
-- 1. 管理员账号由后端首次启动时自动初始化：admin / admin123；
-- 2. 普通用户账号通过前端注册页注册，管理员不开放注册；
-- 3. 商品图片默认上传到后端运行目录下的 uploads 文件夹。


-- ============================================================
-- 线上购物平台 二期功能扩展脚本
-- 新增：商品分类表 / 商品价格表 / 订单状态表 / 商品评论表 / 用户修改记录表
-- 并对现有数据做分类与价格记录回填
-- 执行方式：mysql -u root -p < v2_features.sql
-- ============================================================

USE `shopping_platform`;

-- 1. 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='商品分类表';

-- 2. 商品表增加分类字段
ALTER TABLE `product` ADD COLUMN `category_id` BIGINT NULL COMMENT '商品分类ID' AFTER `cover_image`;

-- 3. 商品价格表（记录每个商品的售价/原价等价格信息）
CREATE TABLE IF NOT EXISTS `product_price` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '价格记录ID',
    `product_id`  BIGINT         NOT NULL COMMENT '商品ID',
    `price_name`  VARCHAR(50)    NOT NULL DEFAULT '售价' COMMENT '价格名称：售价/原价等',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '价格金额',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_price_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='商品价格表';

-- 4. 订单状态表（状态字典）
CREATE TABLE IF NOT EXISTS `order_status` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '状态ID',
    `status_code` VARCHAR(20) NOT NULL COMMENT '状态编码：PAID 已支付 / CANCELLED 已取消 / REFUNDING 退款中 / REFUNDED 已退款',
    `status_name` VARCHAR(50) NOT NULL COMMENT '状态名称',
    `description` VARCHAR(255)         DEFAULT NULL COMMENT '状态说明',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_status_code` (`status_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='订单状态表';

-- 5. 商品评论表
CREATE TABLE IF NOT EXISTS `product_comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `product_id`  BIGINT       NOT NULL COMMENT '商品ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论用户ID',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_comment_product` (`product_id`),
    KEY `idx_comment_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='商品评论表';

-- 6. 用户信息修改记录表（每月修改次数限制）
CREATE TABLE IF NOT EXISTS `user_change_log` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `change_type` VARCHAR(20) NOT NULL COMMENT '修改类型：INFO 个人信息 / PASSWORD 密码',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_change_user` (`user_id`),
    KEY `idx_user_change_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户修改记录表';

-- 7. 填充分类数据
INSERT IGNORE INTO `category` (`name`, `sort`) VALUES
('生活用品', 1),
('运动', 2),
('数码科技', 3);

-- 8. 填充订单状态数据
INSERT IGNORE INTO `order_status` (`status_code`, `status_name`, `description`, `sort`) VALUES
('PAID', '已支付', '订单已支付成功', 1),
('CANCELLED', '已取消', '订单已取消', 2),
('REFUNDING', '退款中', '退款处理中', 3),
('REFUNDED', '已退款', '已退款完成', 4);

-- 9. 为现有商品分配分类
UPDATE `product`
SET `category_id` = (SELECT id FROM `category` WHERE name = '运动')
WHERE `category_id` IS NULL AND `title` LIKE '%羽毛球%';

UPDATE `product`
SET `category_id` = (SELECT id FROM `category` WHERE name = '运动')
WHERE `category_id` IS NULL AND `title` LIKE '%自行车%';

UPDATE `product`
SET `category_id` = (SELECT id FROM `category` WHERE name = '生活用品')
WHERE `category_id` IS NULL AND `title` LIKE '%水杯%';

UPDATE `product`
SET `category_id` = (SELECT id FROM `category` WHERE name = '数码科技')
WHERE `category_id` IS NULL AND `title` LIKE '%Y9000P%';

-- 10. 为现有商品回填价格记录
INSERT INTO `product_price` (`product_id`, `price_name`, `price`)
SELECT p.`id`, '售价', p.`price`
FROM `product` p
WHERE NOT EXISTS (
    SELECT 1 FROM `product_price` pp WHERE pp.`product_id` = p.`id`
);

-- 11. 汇总核对
SELECT 'category' AS tbl, COUNT(*) AS cnt FROM `category`
UNION ALL SELECT 'order_status', COUNT(*) FROM `order_status`
UNION ALL SELECT 'product_price', COUNT(*) FROM `product_price`
UNION ALL SELECT 'product_has_category', COUNT(*) FROM `product` WHERE `category_id` IS NOT NULL;

-- ============================================================
-- v4: 品牌分类功能
-- 新增：brand 表；product 表新增 brand_id；5 个知名品牌；已有商品品牌映射
-- ============================================================

USE `shopping_platform`;

-- 1. 品牌表
CREATE TABLE IF NOT EXISTS `brand` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '品牌名称',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_brand_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='品牌表';

-- 2. 商品表增加品牌字段（可空：生鲜、手作等商品可无品牌）
ALTER TABLE `product` ADD COLUMN `brand_id` BIGINT NULL DEFAULT NULL COMMENT '品牌ID' AFTER `category_id`;
ALTER TABLE `product` ADD KEY `idx_product_brand` (`brand_id`);

-- 3. 插入 5 个知名品牌
INSERT INTO `brand` (`id`, `name`, `sort`) VALUES
    (1, '苹果 Apple',                 1),
    (2, '联想 Lenovo',                2),
    (3, '耐克 Nike',                  3),
    (4, '晨光 M&G',                   4),
    (5, '巴黎欧莱雅 L''Oréal Paris',  5);

-- 4. 已有商品品牌映射
UPDATE `product` SET `brand_id` = 1 WHERE `id` = 13;      -- 苹果蓝牙耳机 AirPods4
UPDATE `product` SET `brand_id` = 2 WHERE `id` = 5;       -- 拯救者 Y9000P
UPDATE `product` SET `brand_id` = 3 WHERE `id` = 16;      -- 经典百搭小白鞋（Nike 经典板鞋）
UPDATE `product` SET `brand_id` = 4 WHERE `id` IN (28, 29); -- A5 皮面笔记本、中性笔套装
UPDATE `product` SET `brand_id` = 5 WHERE `id` IN (18, 19); -- 口红、洁面乳
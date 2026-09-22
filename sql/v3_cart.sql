-- ============================================================
-- 购物车功能扩展脚本
-- 新增：购物车表 cart（用户-商品 唯一，重复加入自动去重）
-- ============================================================

USE `shopping_platform`;

CREATE TABLE IF NOT EXISTS `cart` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '购物车记录ID',
    `user_id`     BIGINT   NOT NULL COMMENT '买家用户ID',
    `product_id`  BIGINT   NOT NULL COMMENT '商品ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_cart_user` (`user_id`),
    KEY `idx_cart_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='购物车表';
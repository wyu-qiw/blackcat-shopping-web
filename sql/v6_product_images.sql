-- ============================================================
-- v6: 商品多图（最多8张）
-- product 新增 images JSON 数组（第一张为封面）；存量商品按封面回填
-- ============================================================

USE `shopping_platform`;

ALTER TABLE `product`
  ADD COLUMN `images` JSON NULL COMMENT '商品图片URL列表(JSON数组)，第一张为封面' AFTER `cover_image`;

UPDATE `product`
SET `images` = JSON_ARRAY(`cover_image`)
WHERE `cover_image` IS NOT NULL
  AND (`images` IS NULL OR JSON_LENGTH(`images`) = 0);
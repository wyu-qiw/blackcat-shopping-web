-- ============================================================
-- v5: 品牌自动识别 + 发布同步
-- 1. brand 增加 keywords（识别关键词，逗号分隔）
-- 2. 补同步商品 33（面部保养套装 → 欧莱雅）
-- ============================================================

USE `shopping_platform`;

-- 1. 品牌识别关键词
ALTER TABLE `brand` ADD COLUMN `keywords` VARCHAR(255) NULL DEFAULT NULL COMMENT '品牌识别关键词，逗号分隔' AFTER `name`;

UPDATE `brand` SET `keywords` = '苹果,Apple,iPhone,iPad,MacBook,Mac,AirPods,iOS'        WHERE `id` = 1;
UPDATE `brand` SET `keywords` = '联想,Lenovo,拯救者,Legion,ThinkPad,小新,天骄,Yoga'    WHERE `id` = 2;
UPDATE `brand` SET `keywords` = '耐克,Nike,AJ,Air Jordan,AirMax,Jordan,飞人'           WHERE `id` = 3;
UPDATE `brand` SET `keywords` = '晨光,M&G,MG,文具,中性笔,按动,皮面笔记本,笔'            WHERE `id` = 4;
UPDATE `brand` SET `keywords` = '欧莱雅,L''Oreal,Loreal,巴黎欧莱雅,护肤,口红,洁面,保湿,面部,保养,套装,精华,面霜,洗面奶' WHERE `id` = 5;

-- 2. 补同步 yuqi 发布的面部保养套装 → 巴黎欧莱雅
UPDATE `product` SET `brand_id` = 5 WHERE `id` = 33 AND `brand_id` IS NULL;
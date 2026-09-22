package com.shop.dto;

import lombok.Data;

/**
 * 管理员移动商品品牌请求
 * brandId 允许为 null：表示将商品移出品牌（设为无品牌）
 */
@Data
public class ProductBrandMoveRequest {

    private Long brandId;
}
package com.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入购物车请求参数
 */
@Data
public class CartAddRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
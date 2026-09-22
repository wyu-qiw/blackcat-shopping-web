package com.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单（购买商品）请求参数
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
}


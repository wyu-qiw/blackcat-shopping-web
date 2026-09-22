package com.shop.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 购物车结算（确认购买）请求参数：仅结算被勾选的商品
 */
@Data
public class CartCheckoutRequest {

    @NotEmpty(message = "请勾选要结算的商品")
    private List<Long> productIds;
}
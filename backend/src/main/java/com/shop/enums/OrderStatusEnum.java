package com.shop.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatusEnum {

    PAID("已支付"),
    CANCELLED("已取消"),
    REFUNDING("退款中"),
    REFUNDED("已退款");

    private final String label;

    OrderStatusEnum(String label) {
        this.label = label;
    }
}

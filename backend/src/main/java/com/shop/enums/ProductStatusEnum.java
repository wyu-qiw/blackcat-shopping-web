package com.shop.enums;

import lombok.Getter;

/**
 * 商品状态枚举：可售 / 已预购 / 无库存
 */
@Getter
public enum ProductStatusEnum {

    ONSALE("可售"),
    RESERVED("已预购"),
    OUT_OF_STOCK("无库存");

    private final String label;

    ProductStatusEnum(String label) {
        this.label = label;
    }

    public static boolean contains(String status) {
        for (ProductStatusEnum item : values()) {
            if (item.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}


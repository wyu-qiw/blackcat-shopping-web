package com.shop.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum RoleEnum {

    USER("普通用户"),
    ADMIN("平台管理员");

    private final String label;

    RoleEnum(String label) {
        this.label = label;
    }
}


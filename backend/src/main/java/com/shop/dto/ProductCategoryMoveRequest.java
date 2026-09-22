package com.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员移动商品分类请求
 */
@Data
public class ProductCategoryMoveRequest {

    @NotNull(message = "请选择目标分类")
    private Long categoryId;
}
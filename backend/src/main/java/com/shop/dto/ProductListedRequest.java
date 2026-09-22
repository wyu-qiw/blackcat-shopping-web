package com.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员上架/下架商品请求参数
 */
@Data
public class ProductListedRequest {

    @NotNull(message = "上架状态不能为空")
    private Boolean listed;
}


package com.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员修改商品状态请求参数
 */
@Data
public class ProductStatusRequest {

    @NotBlank(message = "商品状态不能为空")
    private String status;
}


package com.shop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 售卖者 / 管理员编辑商品请求参数
 */
@Data
public class ProductUpdateRequest {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称最长100个字符")
    private String title;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @NotBlank(message = "商品描述不能为空")
    private String description;

    /** 商品图片（1~8张），第一张为封面 */
    @NotEmpty(message = "请至少保留一张商品图片")
    @Size(max = 8, message = "商品图片最多8张")
    private List<String> images;

    @NotNull(message = "请选择商品分类")
    private Long categoryId;

    /** 品牌ID，可选；未传则自动识别 */
    private Long brandId;
}
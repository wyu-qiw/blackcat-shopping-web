package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体，对应数据库 product 表
 */
@Data
@TableName(value = "product", autoResultMap = true)
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布人（卖家）用户ID */
    private Long sellerId;

    /** 商品名称 */
    private String title;

    /** 商品详情描述 */
    private String description;

    /** 售价 */
    private BigDecimal price;

    /** 商品封面图地址（= images 第一张，冗余便于列表展示） */
    private String coverImage;

    /** 商品图片URL列表（最多8张），第一张为封面 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /** 商品分类ID */
    private Long categoryId;

    /** 品牌ID（可空：生鲜、手作等商品可无品牌） */
    private Long brandId;

    /** 商品状态：ONSALE / RESERVED / OUT_OF_STOCK */
    private String status;

    /** 是否上架：true 上架 / false 下架 */
    private Boolean listed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 卖家昵称，非数据库字段 */
    @TableField(exist = false)
    private String sellerName;

    /** 商品分类名称，非数据库字段 */
    @TableField(exist = false)
    private String categoryName;

    /** 品牌名称，非数据库字段 */
    @TableField(exist = false)
    private String brandName;
}
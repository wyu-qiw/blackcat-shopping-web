package com.shop.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车展示项：购物车记录 + 关联商品的实时信息
 */
@Data
public class CartVO {

    /** 购物车记录ID */
    private Long id;

    private Long productId;

    private String title;

    private String coverImage;

    /** 加入购物车时/当前的商品单价 */
    private BigDecimal price;

    private String sellerName;

    private String categoryName;

    /** 商品是否上架 */
    private Boolean listed;

    /** 商品状态：ONSALE / RESERVED / OUT_OF_STOCK */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
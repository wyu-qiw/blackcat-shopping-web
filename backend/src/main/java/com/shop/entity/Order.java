package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体，对应数据库 orders 表
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 购买的商品ID */
    private Long productId;

    /** 买家用户ID */
    private Long buyerId;

    /** 卖家用户ID */
    private Long sellerId;

    /** 下单金额（购买时的商品价格快照） */
    private BigDecimal amount;

    /** 订单状态 */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 商品标题，非数据库字段 */
    @TableField(exist = false)
    private String productTitle;

    /** 商品封面图，非数据库字段 */
    @TableField(exist = false)
    private String productCover;

    /** 买家昵称，非数据库字段 */
    @TableField(exist = false)
    private String buyerName;

    /** 卖家昵称，非数据库字段 */
    @TableField(exist = false)
    private String sellerName;
}


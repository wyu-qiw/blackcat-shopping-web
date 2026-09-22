package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单状态实体，对应 order_status 表（状态字典）
 */
@Data
@TableName("order_status")
public class OrderStatus {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 状态编码：PAID / CANCELLED / REFUNDING / REFUNDED */
    private String statusCode;

    /** 状态名称 */
    private String statusName;

    /** 状态说明 */
    private String description;

    /** 排序值 */
    private Integer sort;
}

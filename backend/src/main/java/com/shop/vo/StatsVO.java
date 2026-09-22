package com.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员数据大盘统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsVO {

    /** 用户总数 */
    private Long userCount;

    /** 商品总数 */
    private Long productCount;

    /** 在售商品数（上架商品数） */
    private Long onSaleCount;

    /** 已下架商品数 */
    private Long offShelfCount;

    /** 订单数量 */
    private Long orderCount;
}


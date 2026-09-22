package com.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 销售趋势（折线图）单日数据 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesTrendVO {
    /** 日期 YYYY-MM-DD */
    private String date;
    /** 当日订单数 */
    private Long count;
    /** 当日销售额 */
    private BigDecimal amount;
}
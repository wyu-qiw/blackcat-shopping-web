package com.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 通用图表项：name + value（条形图、饼图复用） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartItemVO {
    /** 名称（商品名 / 品类名） */
    private String name;
    /** 数值（销量 / 占比） */
    private Long value;
}
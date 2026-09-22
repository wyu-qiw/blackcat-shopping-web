package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 品牌实体，对应数据库 brand 表
 */
@Data
@TableName("brand")
public class Brand {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 品牌名称 */
    private String name;

    /** 品牌识别关键词，逗号分隔（发布商品时按标题+描述自动识别） */
    private String keywords;

    /** 排序值，越小越靠前 */
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
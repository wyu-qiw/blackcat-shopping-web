package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评论实体，对应 product_comment 表
 */
@Data
@TableName("product_comment")
public class ProductComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 评论用户ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 评论人昵称，非数据库字段 */
    @TableField(exist = false)
    private String nickname;

    /** 评论人头像，非数据库字段 */
    @TableField(exist = false)
    private String avatar;
}

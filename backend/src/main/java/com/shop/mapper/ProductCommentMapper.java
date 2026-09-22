package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.ProductComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评论 Mapper
 */
@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {
}

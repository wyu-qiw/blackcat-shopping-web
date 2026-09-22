package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.ProductPrice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品价格 Mapper
 */
@Mapper
public interface ProductPriceMapper extends BaseMapper<ProductPrice> {
}

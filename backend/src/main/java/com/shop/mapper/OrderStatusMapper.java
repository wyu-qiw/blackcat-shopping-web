package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.OrderStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单状态 Mapper
 */
@Mapper
public interface OrderStatusMapper extends BaseMapper<OrderStatus> {
}

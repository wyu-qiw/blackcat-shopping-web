package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.UserChangeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户修改记录 Mapper
 */
@Mapper
public interface UserChangeLogMapper extends BaseMapper<UserChangeLog> {
}

package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.entity.Brand;
import com.shop.mapper.BrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 品牌服务
 */
@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandMapper brandMapper;

    /**
     * 全部品牌，按排序值升序
     */
    public List<Brand> list() {
        return brandMapper.selectList(
                new LambdaQueryWrapper<Brand>().orderByAsc(Brand::getSort)
        );
    }
}
package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.Brand;
import com.shop.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 品牌接口（公开）
 */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public Result<List<Brand>> list() {
        return Result.success(brandService.list());
    }
}
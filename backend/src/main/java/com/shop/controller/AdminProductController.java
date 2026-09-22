package com.shop.controller;

import com.shop.common.Result;
import com.shop.dto.ProductBrandMoveRequest;
import com.shop.dto.ProductCategoryMoveRequest;
import com.shop.dto.ProductListedRequest;
import com.shop.dto.ProductStatusRequest;
import com.shop.entity.Product;
import com.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员商品管理接口：全平台商品、上架/下架、修改状态、移动分类 / 品牌
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    /**
     * 全平台商品列表（包含已下架商品）
     * 可按分类 categoryId / 品牌 brandId 过滤（均为可选，不传则返回全部）
     */
    @GetMapping
    public Result<List<Product>> listAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId) {
        return Result.success(productService.adminListAll(categoryId, brandId));
    }

    /**
     * 修改商品状态：ONSALE 可售 / RESERVED 已预购 / OUT_OF_STOCK 无库存
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid ProductStatusRequest request) {
        productService.changeStatus(id, request.getStatus());
        return Result.success("商品状态修改成功", null);
    }

    /**
     * 上架 / 下架商品
     */
    @PutMapping("/{id}/listed")
    public Result<Void> updateListed(@PathVariable Long id, @RequestBody @Valid ProductListedRequest request) {
        productService.changeListed(id, request.getListed());
        return Result.success(request.getListed() ? "商品已上架" : "商品已下架", null);
    }

    /**
     * 移动商品到其他商品分类（不影响品牌）
     */
    @PutMapping("/{id}/category")
    public Result<Void> moveCategory(@PathVariable Long id,
                                     @RequestBody @Valid ProductCategoryMoveRequest request) {
        productService.moveCategory(id, request.getCategoryId());
        return Result.success("商品分类已调整", null);
    }

    /**
     * 移动商品到其他品牌；brandId 为 null 表示移出品牌（不影响分类）
     */
    @PutMapping("/{id}/brand")
    public Result<Void> moveBrand(@PathVariable Long id,
                                  @RequestBody @Valid ProductBrandMoveRequest request) {
        productService.moveBrand(id, request.getBrandId());
        return Result.success("商品品牌已调整", null);
    }
}
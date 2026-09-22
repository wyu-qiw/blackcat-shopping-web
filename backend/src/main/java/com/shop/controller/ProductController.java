package com.shop.controller;

import com.shop.common.Result;
import com.shop.dto.CommentCreateRequest;
import com.shop.dto.ProductListedRequest;
import com.shop.dto.ProductPublishRequest;
import com.shop.dto.ProductUpdateRequest;
import com.shop.entity.Product;
import com.shop.entity.ProductComment;
import com.shop.service.ProductCommentService;
import com.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端商品接口：浏览、详情、发布、我的商品
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final ProductCommentService productCommentService;

    /**
     * 前台商品列表：仅返回已上架商品
     */
    @GetMapping("/products")
    public Result<List<Product>> list(@RequestParam(required = false) String keyword) {
        return Result.success(productService.listPublic(keyword));
    }

    /**
     * 商品详情：下架商品仅管理员可见
     */
    @GetMapping("/products/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    /**
     * 用户发布商品（需登录）
     */
    @PostMapping("/products")
    public Result<Product> publish(@RequestBody @Valid ProductPublishRequest request) {
        return Result.success("发布成功", productService.publish(request));
    }

    /**
     * 我发布的商品（需登录）
     */
    @GetMapping("/products/mine")
    public Result<List<Product>> mine() {
        return Result.success(productService.listMine());
    }

    /**
     * 售卖者（或管理员）编辑自己上架的商品
     */
    @PutMapping("/products/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody @Valid ProductUpdateRequest request) {
        return Result.success("商品信息修改成功", productService.update(id, request));
    }

    /**
     * 售卖者（或管理员）上架 / 下架自己上架的商品
     */
    @PutMapping("/products/{id}/listed")
    public Result<Void> updateListed(@PathVariable Long id, @RequestBody @Valid ProductListedRequest request) {
        productService.changeListedForOwner(id, request.getListed());
        return Result.success(request.getListed() ? "商品已上架" : "商品已下架", null);
    }

    /**
     * 商品评论列表
     */
    @GetMapping("/products/{id}/comments")
    public Result<List<ProductComment>> comments(@PathVariable Long id) {
        return Result.success(productCommentService.listByProduct(id));
    }

    /**
     * 发表商品评论（需登录）
     */
    @PostMapping("/products/{id}/comments")
    public Result<ProductComment> createComment(@PathVariable Long id, @RequestBody @Valid CommentCreateRequest request) {
        return Result.success("评论成功", productCommentService.create(id, request.getContent()));
    }

    /**
     * 售卖者（或管理员）删除自己上架的商品
     */
    @DeleteMapping("/products/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success("商品删除成功", null);
    }
}

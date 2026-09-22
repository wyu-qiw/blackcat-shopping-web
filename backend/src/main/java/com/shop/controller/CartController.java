package com.shop.controller;

import com.shop.common.Result;
import com.shop.dto.CartAddRequest;
import com.shop.dto.CartCheckoutRequest;
import com.shop.entity.Order;
import com.shop.service.CartService;
import com.shop.vo.CartVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端购物车接口（均需登录）
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 加入购物车（商品详情页“+购物车”）
     */
    @PostMapping("/add")
    public Result<CartVO> add(@RequestBody @Valid CartAddRequest request) {
        return Result.success("已加入购物车", cartService.add(request.getProductId()));
    }

    /**
     * 我的购物车
     */
    @GetMapping
    public Result<List<CartVO>> list() {
        return Result.success(cartService.list());
    }

    /**
     * 删除购物车中的单个商品
     */
    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        cartService.remove(productId);
        return Result.success("已移除", null);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping
    public Result<Void> clear() {
        cartService.clear();
        return Result.success("购物车已清空", null);
    }

    /**
     * 勾选结算：仅结算被勾选的商品，生成订单并移出购物车
     */
    @PostMapping("/checkout")
    public Result<List<Order>> checkout(@RequestBody @Valid CartCheckoutRequest request) {
        return Result.success("购买成功", cartService.checkout(request.getProductIds()));
    }
}
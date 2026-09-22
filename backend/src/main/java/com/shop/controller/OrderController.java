package com.shop.controller;

import com.shop.common.Result;
import com.shop.dto.CreateOrderRequest;
import com.shop.entity.Order;
import com.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端订单接口：购买商品、我的购买订单
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 购买商品并生成订单（需登录）
     */
    @PostMapping
    public Result<Order> create(@RequestBody @Valid CreateOrderRequest request) {
        return Result.success("购买成功", orderService.createOrder(request.getProductId()));
    }

    /**
     * 我的购买订单（需登录）
     */
    @GetMapping("/mine")
    public Result<List<Order>> mine() {
        return Result.success(orderService.listMine());
    }

    /**
     * 买家取消订单（已支付 -> 已取消，商品恢复可售）
     */
    @PutMapping("/{id}/cancel")
    public Result<Order> cancel(@PathVariable Long id) {
        return Result.success("订单已取消", orderService.cancelOrder(id));
    }

    /**
     * 买家申请退款（已支付 -> 退款中，等待管理员处理）
     */
    @PutMapping("/{id}/refund")
    public Result<Order> refund(@PathVariable Long id) {
        return Result.success("退款申请已提交，等待管理员处理", orderService.requestRefund(id));
    }
}

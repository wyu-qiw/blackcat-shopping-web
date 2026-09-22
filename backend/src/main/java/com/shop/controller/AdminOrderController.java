package com.shop.controller;

import com.shop.common.Result;
import com.shop.dto.RefundHandleRequest;
import com.shop.entity.Order;
import com.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员订单接口：全平台订单列表、撤销、删除、退款处理
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<List<Order>> listAll() {
        return Result.success(orderService.adminListAll());
    }

    /**
     * 管理员处理退款申请：true 同意（已退款，商品恢复可售）/ false 驳回（恢复已支付）
     */
    @PutMapping("/{id}/refund")
    public Result<Order> handleRefund(@PathVariable Long id, @RequestBody @Valid RefundHandleRequest request) {
        return Result.success(request.getApproved() ? "退款已通过" : "退款已驳回",
                orderService.handleRefund(id, request.getApproved()));
    }

    /**
     * 管理员撤销订单：将"已支付"或"已退款"的订单改为"已取消"，并恢复商品为可售
     */
    @PutMapping("/{id}/cancel")
    public Result<Order> cancelOrder(@PathVariable Long id) {
        return Result.success("订单已撤销，商品已恢复可售", orderService.adminCancel(id));
    }

    /**
     * 管理员删除订单：仅允许删除"已取消"或"已退款"的订单（物理删除，不可恢复）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.adminDelete(id);
        return Result.success("订单已删除", null);
    }
}
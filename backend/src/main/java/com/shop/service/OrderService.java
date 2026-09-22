package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.AuthContext;
import com.shop.common.BizException;
import com.shop.entity.Order;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.enums.OrderStatusEnum;
import com.shop.enums.ProductStatusEnum;
import com.shop.mapper.OrderMapper;
import com.shop.mapper.ProductMapper;
import com.shop.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务：购买商品、我的订单、全平台订单
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

    private final ProductMapper productMapper;

    private final UserMapper userMapper;

    /**
     * 购买商品：校验状态后生成订单，并把商品改为“已预购”
     */
    @Transactional
    public Order createOrder(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        if (!product.getListed()) {
            throw new BizException("商品已下架，无法购买");
        }
        if (!ProductStatusEnum.ONSALE.name().equals(product.getStatus())) {
            throw new BizException("商品当前状态不可购买");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProductId(product.getId());
        order.setBuyerId(AuthContext.getUserId());
        order.setSellerId(product.getSellerId());
        order.setAmount(product.getPrice());
        order.setStatus(OrderStatusEnum.PAID.name());
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 下单成功后商品变为“已预购”，禁止其他用户再次购买
        product.setStatus(ProductStatusEnum.RESERVED.name());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);

        return order;
    }

    /**
     * 买家取消订单：已支付 -> 已取消，商品恢复可售
     */
    public Order cancelOrder(Long id) {
        Order order = requireOwnOrder(id);
        requireStatus(order, OrderStatusEnum.PAID);
        order.setStatus(OrderStatusEnum.CANCELLED.name());
        orderMapper.updateById(order);
        restoreProductOnSale(order.getProductId());
        return order;
    }

    /**
     * 买家申请退款：已支付 -> 退款中，等待管理员处理
     */
    public Order requestRefund(Long id) {
        Order order = requireOwnOrder(id);
        requireStatus(order, OrderStatusEnum.PAID);
        order.setStatus(OrderStatusEnum.REFUNDING.name());
        orderMapper.updateById(order);
        return order;
    }

    /**
     * 管理员处理退款申请：同意 -> 已退款（商品恢复可售）；驳回 -> 恢复已支付
     */
    public Order handleRefund(Long id, boolean approved) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        requireStatus(order, OrderStatusEnum.REFUNDING);
        if (approved) {
            order.setStatus(OrderStatusEnum.REFUNDED.name());
            orderMapper.updateById(order);
            restoreProductOnSale(order.getProductId());
        } else {
            order.setStatus(OrderStatusEnum.PAID.name());
            orderMapper.updateById(order);
        }
        return order;
    }

    /**
     * 我的购买订单
     */
    public List<Order> listMine() {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getBuyerId, AuthContext.getUserId())
                        .orderByDesc(Order::getCreateTime)
        );
        fillProductInfo(orders);
        return orders;
    }

    /**
     * 管理员查看全平台订单
     */
    public List<Order> adminListAll() {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime)
        );
        fillOrderDetail(orders);
        return orders;
    }

    private void fillProductInfo(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Long> productIds = orders.stream().map(Order::getProductId).distinct().toList();
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        orders.forEach(order -> {
            Product product = productMap.get(order.getProductId());
            if (product != null) {
                order.setProductTitle(product.getTitle());
                order.setProductCover(product.getCoverImage());
            }
        });
    }

    private void fillOrderDetail(List<Order> orders) {
        fillProductInfo(orders);
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Long> userIds = orders.stream()
                .flatMap(order -> java.util.stream.Stream.of(order.getBuyerId(), order.getSellerId()))
                .distinct()
                .toList();
        Map<Long, String> nameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
        orders.forEach(order -> {
            order.setBuyerName(nameMap.getOrDefault(order.getBuyerId(), "未知用户"));
            order.setSellerName(nameMap.getOrDefault(order.getSellerId(), "未知用户"));
        });
    }

    /**
     * 校验订单属于当前登录买家
     */
    private Order requireOwnOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        Long userId = AuthContext.getUserId();
        if (userId == null || !userId.equals(order.getBuyerId())) {
            throw new BizException(403, "只能操作自己的订单");
        }
        return order;
    }

    /**
     * 校验订单当前状态
     */
    private void requireStatus(Order order, OrderStatusEnum expected) {
        if (!expected.name().equals(order.getStatus())) {
            throw new BizException("当前订单状态不允许该操作");
        }
    }

    /**
     * 管理员撤销订单：仅允许对"已支付"或\"已退款\"状态的订单撤销。
     * 撤销后：订单状态置为\"已取消\"，关联商品恢复"可售"状态（若已下架则保持下架）。
     */
    @Transactional
    public Order adminCancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        String status = order.getStatus();
        if (OrderStatusEnum.CANCELLED.name().equals(status)) {
            throw new BizException("订单已处于取消状态，无需重复撤销");
        }
        if (OrderStatusEnum.REFUNDING.name().equals(status)) {
            throw new BizException("退款中的订单请先处理退款申请");
        }
        // 允许从 PAID / REFUNDED 撤销到 CANCELLED
        order.setStatus(OrderStatusEnum.CANCELLED.name());
        orderMapper.updateById(order);
        // 恢复商品为可售（如果商品存在）
        restoreProductOnSale(order.getProductId());
        return order;
    }

    /**
     * 管理员物理删除订单：仅允许删除\"已取消\"或\"已退款\"的订单，避免误删有效交易。
     */
    public void adminDelete(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        String status = order.getStatus();
        if (!(OrderStatusEnum.CANCELLED.name().equals(status) || OrderStatusEnum.REFUNDED.name().equals(status))) {
            throw new BizException("仅允许删除\"已取消\"或\"已退款\"的订单，请先撤销该订单");
        }
        orderMapper.deleteById(id);
    }

    /**
     * 订单取消 / 退款完成后，商品恢复为可售
     */
    private void restoreProductOnSale(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setStatus(ProductStatusEnum.ONSALE.name());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);
        }
    }

    private String generateOrderNo() {
        int random = ThreadLocalRandom.current().nextInt(10000);
        return "SO" + System.currentTimeMillis() + String.format("%04d", random);
    }
}

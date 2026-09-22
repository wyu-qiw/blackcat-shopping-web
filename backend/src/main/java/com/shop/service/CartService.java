package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.AuthContext;
import com.shop.common.BizException;
import com.shop.entity.Cart;
import com.shop.entity.Category;
import com.shop.entity.Order;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.enums.OrderStatusEnum;
import com.shop.enums.ProductStatusEnum;
import com.shop.mapper.CartMapper;
import com.shop.mapper.CategoryMapper;
import com.shop.mapper.OrderMapper;
import com.shop.mapper.ProductMapper;
import com.shop.mapper.UserMapper;
import com.shop.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 购物车服务：加入 / 批量加入 / 列表 / 删除 / 勾选结算
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;

    private final ProductMapper productMapper;

    private final UserMapper userMapper;

    private final CategoryMapper categoryMapper;

    private final OrderMapper orderMapper;

    /**
     * 加入购物车：同一用户对同一商品只保留一条，重复加入不产生重复记录
     */
    public CartVO add(Long productId) {
        Long userId = requireUser();
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        if (!Boolean.TRUE.equals(product.getListed())) {
            throw new BizException("商品已下架，无法加入购物车");
        }
        Long exist = cartMapper.selectCount(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, productId)
        );
        if (exist == null || exist == 0) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setCreateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }
        return list().stream()
                .filter(vo -> Objects.equals(vo.getProductId(), productId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 当前用户购物车（含商品实时信息，按加入时间倒序）
     */
    public List<CartVO> list() {
        Long userId = requireUser();
        List<Cart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .orderByDesc(Cart::getCreateTime)
        );
        if (carts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> productIds = carts.stream().map(Cart::getProductId).distinct().toList();
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 卖家昵称
        List<Long> sellerIds = productMap.values().stream()
                .map(Product::getSellerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> sellerMap = sellerIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(sellerIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getNickname));

        // 分类名称
        List<Long> categoryIds = productMap.values().stream()
                .map(Product::getCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> categoryMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));

        List<CartVO> result = new ArrayList<>();
        for (Cart cart : carts) {
            Product product = productMap.get(cart.getProductId());
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setCreateTime(cart.getCreateTime());
            if (product != null) {
                vo.setTitle(product.getTitle());
                vo.setCoverImage(product.getCoverImage());
                vo.setPrice(product.getPrice());
                vo.setSellerName(sellerMap.getOrDefault(product.getSellerId(), "未知卖家"));
                vo.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), "未分类"));
                vo.setListed(product.getListed());
                vo.setStatus(product.getStatus());
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 删除购物车中的指定商品（单个，按商品ID）
     */
    public void remove(Long productId) {
        Long userId = requireUser();
        cartMapper.delete(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, productId)
        );
    }

    /**
     * 清空当前用户购物车
     */
    public void clear() {
        Long userId = requireUser();
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }

    /**
     * 购物车勾选结算：为每个勾选且可售的商品生成订单，并从购物车移除。
     * 合计金额由后端按商品实时价格计算，避免前端篡改价格。
     */
    @Transactional
    public List<Order> checkout(List<Long> productIds) {
        Long userId = requireUser();
        if (productIds == null || productIds.isEmpty()) {
            throw new BizException("请勾选要结算的商品");
        }
        Set<Long> idSet = productIds.stream().collect(Collectors.toSet());

        // 取当前用户购物车中被勾选的记录
        List<Cart> selected = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .in(Cart::getProductId, idSet)
        );
        if (selected.isEmpty()) {
            throw new BizException("勾选的商品不在购物车中");
        }

        Map<Long, Product> productMap = productMapper.selectBatchIds(
                        selected.stream().map(Cart::getProductId).distinct().toList()).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Order> orders = new ArrayList<>();
        List<Long> purchasedProductIds = new ArrayList<>();

        for (Cart cart : selected) {
            Product product = productMap.get(cart.getProductId());
            if (product == null) {
                continue;
            }
            boolean purchasable = Boolean.TRUE.equals(product.getListed())
                    && ProductStatusEnum.ONSALE.name().equals(product.getStatus());
            if (!purchasable) {
                continue;
            }

            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setProductId(product.getId());
            order.setBuyerId(userId);
            order.setSellerId(product.getSellerId());
            order.setAmount(product.getPrice());
            order.setStatus(OrderStatusEnum.PAID.name());
            order.setCreateTime(LocalDateTime.now());
            orderMapper.insert(order);

            product.setStatus(ProductStatusEnum.RESERVED.name());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            orders.add(order);
            purchasedProductIds.add(product.getId());
        }

        if (orders.isEmpty()) {
            // 没有任何可成交商品：整体回滚（此处也未产生订单），提示用户
            throw new BizException("勾选的商品均不可购买（已下架或已被预购）");
        }

        // 已成功下单的商品从购物车移除；不可购买的商品保留在购物车中
        cartMapper.delete(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .in(Cart::getProductId, purchasedProductIds)
        );
        return orders;
    }

    private Long requireUser() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new BizException(401, "请先登录");
        }
        return userId;
    }

    private String generateOrderNo() {
        int random = ThreadLocalRandom.current().nextInt(10000);
        return "SO" + System.currentTimeMillis() + String.format("%04d", random);
    }
}
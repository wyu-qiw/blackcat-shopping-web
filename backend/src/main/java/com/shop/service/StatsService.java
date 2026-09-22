package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.entity.Category;
import com.shop.entity.Order;
import com.shop.entity.Product;
import com.shop.enums.OrderStatusEnum;
import com.shop.mapper.CategoryMapper;
import com.shop.mapper.OrderMapper;
import com.shop.mapper.ProductMapper;
import com.shop.mapper.UserMapper;
import com.shop.vo.ChartItemVO;
import com.shop.vo.SalesTrendVO;
import com.shop.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台数据统计服务
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;

    public StatsVO getStats() {
        Long userCount = userMapper.selectCount(null);
        Long productCount = productMapper.selectCount(null);
        Long onSaleCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getListed, true)
        );
        Long offShelfCount = productCount - onSaleCount;
        Long orderCount = orderMapper.selectCount(null);
        return new StatsVO(userCount, productCount, onSaleCount, offShelfCount, orderCount);
    }

    /**
     * 销售订单的统一定义：已支付 / 退款中 / 已退款 都算发生过销售，已取消的不算
     */
    private List<Order> salesOrders() {
        return orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .ne(Order::getStatus, OrderStatusEnum.CANCELLED.name())
        );
    }

    /**
     * 条形图：商品销量 TopN（按有效订单数倒序）
     */
    public List<ChartItemVO> getProductSalesTopN(int limit) {
        if (limit <= 0) limit = 8;
        List<Order> orders = salesOrders();
        // 商品ID -> 销量
        Map<Long, Long> productSales = orders.stream()
                .collect(Collectors.groupingBy(Order::getProductId, Collectors.counting()));
        // 商品ID -> 商品
        Map<Long, Product> productMap = productMapper.selectBatchIds(productSales.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        return productSales.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Product p = productMap.get(e.getKey());
                    String name = p != null ? p.getTitle() : "已删除商品";
                    if (name.length() > 12) name = name.substring(0, 12) + "…";
                    return new ChartItemVO(name, e.getValue());
                })
                .collect(Collectors.toList());
    }

    /**
     * 折线图：近 N 天每日销售趋势（含订单数与销售额），0 销售的日子也补 0
     */
    public List<SalesTrendVO> getDailySalesTrend(int days) {
        if (days <= 0 || days > 90) days = 7;
        List<Order> orders = salesOrders();
        // 日期字符串 -> 累计
        Map<String, long[]> agg = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            agg.put(today.minusDays(i).toString(), new long[]{0, 0});
        }
        for (Order o : orders) {
            LocalDateTime ct = o.getCreateTime();
            if (ct == null) continue;
            String key = ct.toLocalDate().toString();
            long[] bucket = agg.get(key);
            if (bucket != null) {
                bucket[0] += 1;
                bucket[1] += o.getAmount() == null ? 0L : o.getAmount().longValue() * 100; // 转为分再求和，避免精度
            }
        }
        List<SalesTrendVO> result = new ArrayList<>();
        for (Map.Entry<String, long[]> e : agg.entrySet()) {
            result.add(new SalesTrendVO(
                    e.getKey(),
                    e.getValue()[0],
                    BigDecimal.valueOf(e.getValue()[1]).divide(BigDecimal.valueOf(100))
            ));
        }
        return result;
    }

    /**
     * 饼图：各品类销售占比（按有效订单数）
     */
    public List<ChartItemVO> getCategoryDistribution() {
        List<Order> orders = salesOrders();
        Map<Long, Long> productSales = orders.stream()
                .collect(Collectors.groupingBy(Order::getProductId, Collectors.counting()));
        if (productSales.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Product> productMap = productMapper.selectBatchIds(productSales.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, Long> categorySales = new HashMap<>();
        for (Map.Entry<Long, Long> e : productSales.entrySet()) {
            Product p = productMap.get(e.getKey());
            if (p == null || p.getCategoryId() == null) continue;
            categorySales.merge(p.getCategoryId(), e.getValue(), Long::sum);
        }
        Map<Long, String> categoryNames = categoryMapper.selectBatchIds(categorySales.keySet()).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        return categorySales.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(e -> new ChartItemVO(
                        categoryNames.getOrDefault(e.getKey(), "未分类"),
                        e.getValue()
                ))
                .collect(Collectors.toList());
    }
}
package com.shop.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.entity.Category;
import com.shop.entity.OrderStatus;
import com.shop.entity.Product;
import com.shop.entity.ProductPrice;
import com.shop.entity.User;
import com.shop.enums.RoleEnum;
import com.shop.mapper.CategoryMapper;
import com.shop.mapper.OrderStatusMapper;
import com.shop.mapper.ProductMapper;
import com.shop.mapper.ProductPriceMapper;
import com.shop.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 启动初始化：首次运行自动创建默认管理员账号（admin / admin123）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    private final CategoryMapper categoryMapper;

    private final OrderStatusMapper orderStatusMapper;

    private final ProductMapper productMapper;

    private final ProductPriceMapper productPriceMapper;

    @Override
    public void run(String... args) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")
        );
        if (count == null || count == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("平台管理员");
            admin.setRole(RoleEnum.ADMIN.name());
            admin.setCreateTime(LocalDateTime.now());
            userMapper.insert(admin);
            log.info("已初始化默认管理员账号：admin / admin123");
        }

        initCategories();
        initOrderStatuses();
        backfillProductPrice();
    }

    /**
     * 初始化默认商品分类
     */
    private void initCategories() {
        String[][] categories = {
                {"生活用品", "1"},
                {"运动", "2"},
                {"数码科技", "3"},
                {"食品生鲜", "4"},
                {"服饰鞋包", "5"},
                {"美妆个护", "6"},
                {"图书文娱", "7"},
                {"家居家装", "8"},
                {"母婴玩具", "9"},
                {"办公用品", "10"}
        };
        for (String[] item : categories) {
            Long exist = categoryMapper.selectCount(
                    new LambdaQueryWrapper<Category>().eq(Category::getName, item[0])
            );
            if (exist == null || exist == 0) {
                Category category = new Category();
                category.setName(item[0]);
                category.setSort(Integer.parseInt(item[1]));
                categoryMapper.insert(category);
                log.info("已初始化商品分类：{}", item[0]);
            }
        }
    }

    /**
     * 初始化订单状态字典
     */
    private void initOrderStatuses() {
        String[][] statuses = {
                {"PAID", "已支付", "订单已支付成功", "1"},
                {"CANCELLED", "已取消", "订单已取消", "2"},
                {"REFUNDING", "退款中", "退款处理中", "3"},
                {"REFUNDED", "已退款", "已退款完成", "4"}
        };
        for (String[] item : statuses) {
            Long exist = orderStatusMapper.selectCount(
                    new LambdaQueryWrapper<OrderStatus>().eq(OrderStatus::getStatusCode, item[0])
            );
            if (exist == null || exist == 0) {
                OrderStatus status = new OrderStatus();
                status.setStatusCode(item[0]);
                status.setStatusName(item[1]);
                status.setDescription(item[2]);
                status.setSort(Integer.parseInt(item[3]));
                orderStatusMapper.insert(status);
                log.info("已初始化订单状态：{}", item[1]);
            }
        }
    }

    /**
     * 为尚未记录价格的商品回填售价记录
     */
    private void backfillProductPrice() {
        List<Product> products = productMapper.selectList(null);
        if (products == null || products.isEmpty()) {
            return;
        }
        for (Product product : products) {
            Long exist = productPriceMapper.selectCount(
                    new LambdaQueryWrapper<ProductPrice>()
                            .eq(ProductPrice::getProductId, product.getId())
                            .eq(ProductPrice::getPriceName, "售价")
            );
            if (exist == null || exist == 0) {
                ProductPrice price = new ProductPrice();
                price.setProductId(product.getId());
                price.setPriceName("售价");
                price.setPrice(product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
                price.setCreateTime(LocalDateTime.now());
                productPriceMapper.insert(price);
            }
        }
    }
}

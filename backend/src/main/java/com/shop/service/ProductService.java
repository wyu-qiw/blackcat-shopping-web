package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shop.common.AuthContext;
import com.shop.common.BizException;
import com.shop.dto.ProductPublishRequest;
import com.shop.dto.ProductUpdateRequest;
import com.shop.entity.Brand;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.entity.ProductComment;
import com.shop.entity.ProductPrice;
import com.shop.entity.User;
import com.shop.enums.ProductStatusEnum;
import com.shop.mapper.BrandMapper;
import com.shop.mapper.CategoryMapper;
import com.shop.mapper.ProductCommentMapper;
import com.shop.mapper.ProductMapper;
import com.shop.mapper.ProductPriceMapper;
import com.shop.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务：浏览、发布（品牌自动识别）、个人商品、后台管理
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    private final UserMapper userMapper;

    private final CategoryMapper categoryMapper;

    private final BrandMapper brandMapper;

    private final ProductPriceMapper productPriceMapper;

    private final ProductCommentMapper productCommentMapper;

    /**
     * 前台首页：只展示已上架商品
     */
    public List<Product> listPublic(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getListed, true);
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Product::getTitle, kw).or().like(Product::getDescription, kw));
        }
        wrapper.orderByDesc(Product::getCreateTime);
        List<Product> products = productMapper.selectList(wrapper);
        fillSellerName(products);
        fillCategoryName(products);
        fillBrandName(products);
        return products;
    }

    /**
     * 商品详情：未上架商品仅管理员可见
     */
    public Product getDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        if (!product.getListed() && !AuthContext.isAdmin()) {
            throw new BizException(404, "商品不存在或已下架");
        }
        fillSellerName(List.of(product));
        fillCategoryName(List.of(product));
        fillBrandName(List.of(product));
        return product;
    }

    /**
     * 用户发布商品：默认上架、状态为可售。
     * 品牌：前端显式选择则用所选，否则按标题+描述关键词自动识别。
     */
    public Product publish(ProductPublishRequest request) {
        Product product = new Product();
        product.setSellerId(AuthContext.getUserId());
        product.setTitle(request.getTitle());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImages(request.getImages());
        product.setCoverImage(firstImage(request.getImages()));
        product.setCategoryId(request.getCategoryId());
        Long brandId = request.getBrandId() != null
                ? request.getBrandId()
                : detectBrand(request.getTitle(), request.getDescription());
        product.setBrandId(brandId);
        product.setStatus(ProductStatusEnum.ONSALE.name());
        product.setListed(true);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
        recordPrice(product.getId(), "售价", request.getPrice());
        return product;
    }

    /**
     * 售卖者（或管理员）编辑自己上架的商品信息。
     * 品牌：显式传入（含置空）以传入为准；未传则重新自动识别。
     */
    public Product update(Long id, ProductUpdateRequest request) {
        Product product = requireProduct(id);
        checkOwnerOrAdmin(product);

        boolean priceChanged = request.getPrice() != null
                && !request.getPrice().equals(product.getPrice());
        Long brandId = request.getBrandId() != null
                ? request.getBrandId()
                : detectBrand(request.getTitle(), request.getDescription());

        // 1) updateById：实体配置 autoResultMap，images 会经 JacksonTypeHandler 正确写为 JSON
        //    （LambdaUpdateWrapper.set 不会自动应用 TypeHandler，会把图片数组写坏）。
        product.setTitle(request.getTitle());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImages(request.getImages());
        product.setCoverImage(firstImage(request.getImages()));
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(brandId);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);

        // 2) updateById 默认忽略 null 字段；若识别/选择为“无品牌”，用 wrapper 显式置 NULL。
        if (brandId == null) {
            productMapper.update(null, new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, id)
                    .set(Product::getBrandId, null));
        }

        if (priceChanged) {
            recordPrice(id, "售价", request.getPrice());
        }
        fillCategoryName(List.of(product));
        fillBrandName(List.of(product));
        return product;
    }

    /**
     * 售卖者（或管理员）上架 / 下架自己上架的商品
     */
    public void changeListedForOwner(Long id, Boolean listed) {
        Product product = requireProduct(id);
        checkOwnerOrAdmin(product);
        product.setListed(listed);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    /**
     * 售卖者（或管理员）删除自己上架的商品，并清理其价格记录与评论
     */
    public void delete(Long id) {
        Product product = requireProduct(id);
        checkOwnerOrAdmin(product);
        productPriceMapper.delete(
                new LambdaQueryWrapper<ProductPrice>().eq(ProductPrice::getProductId, id)
        );
        productCommentMapper.delete(
                new LambdaQueryWrapper<ProductComment>().eq(ProductComment::getProductId, id)
        );
        productMapper.deleteById(id);
    }

    /**
     * 我发布的商品
     */
    public List<Product> listMine() {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getSellerId, AuthContext.getUserId())
                        .orderByDesc(Product::getCreateTime)
        );
        fillCategoryName(products);
        fillBrandName(products);
        return products;
    }

    /**
     * 管理员查看全平台商品（包含已下架），可按分类 / 品牌过滤
     */
    public List<Product> adminListAll(Long categoryId, Long brandId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreateTime);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (brandId != null) {
            wrapper.eq(Product::getBrandId, brandId);
        }
        List<Product> products = productMapper.selectList(wrapper);
        fillSellerName(products);
        fillCategoryName(products);
        fillBrandName(products);
        return products;
    }

    /**
     * 管理员修改商品状态：可售 / 已预购 / 无库存
     */
    public void changeStatus(Long id, String status) {
        if (!ProductStatusEnum.contains(status)) {
            throw new BizException("商品状态不合法");
        }
        Product product = requireProduct(id);
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    /**
     * 管理员上架 / 下架商品
     */
    public void changeListed(Long id, Boolean listed) {
        Product product = requireProduct(id);
        product.setListed(listed);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    /**
     * 管理员把商品移动到其他商品分类（不影响品牌）。
     * 用 LambdaUpdateWrapper 显式 set，避免 updateById 忽略 null 字段。
     */
    public void moveCategory(Long id, Long categoryId) {
        requireProduct(id);
        if (categoryId != null && categoryMapper.selectById(categoryId) == null) {
            throw new BizException("目标分类不存在");
        }
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .set(Product::getCategoryId, categoryId)
                .set(Product::getUpdateTime, LocalDateTime.now());
        productMapper.update(null, wrapper);
    }

    /**
     * 管理员把商品移动到其他品牌（brandId 为 null 表示移出品牌 / 无品牌；不影响分类）。
     * 用 LambdaUpdateWrapper 显式 set，确保能真正把 brand_id 写为 NULL
     * （updateById 默认忽略 null 字段，会导致“清空品牌”不生效）。
     */
    public void moveBrand(Long id, Long brandId) {
        requireProduct(id);
        if (brandId != null && brandMapper.selectById(brandId) == null) {
            throw new BizException("目标品牌不存在");
        }
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .set(Product::getBrandId, brandId)
                .set(Product::getUpdateTime, LocalDateTime.now());
        productMapper.update(null, wrapper);
    }

    /**
     * 根据品牌识别关键词，从商品标题 + 描述中自动判断品牌。
     * 规则：标题命中优先于描述命中；同级别按品牌 sort 顺序取第一个；都不命中返回 null。
     */
    private Long detectBrand(String title, String description) {
        List<Brand> brands = brandMapper.selectList(
                new LambdaQueryWrapper<Brand>().orderByAsc(Brand::getSort)
        );
        if (brands == null || brands.isEmpty()) {
            return null;
        }
        String t = title == null ? "" : title.toLowerCase(Locale.ROOT);
        String d = description == null ? "" : description.toLowerCase(Locale.ROOT);
        Long descMatch = null;
        for (Brand brand : brands) {
            if (!StringUtils.hasText(brand.getKeywords())) {
                continue;
            }
            for (String raw : brand.getKeywords().split(",")) {
                String kw = raw.trim().toLowerCase(Locale.ROOT);
                if (kw.isEmpty()) {
                    continue;
                }
                if (t.contains(kw)) {
                    return brand.getId();
                }
                if (descMatch == null && d.contains(kw)) {
                    descMatch = brand.getId();
                }
            }
        }
        return descMatch;
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        return product;
    }

    /**
     * 校验当前登录用户是否为商品售卖者或管理员
     */
    private void checkOwnerOrAdmin(Product product) {
        Long userId = AuthContext.getUserId();
        if (AuthContext.isAdmin()) {
            return;
        }
        if (userId == null || !userId.equals(product.getSellerId())) {
            throw new BizException(403, "只能操作自己上架的商品");
        }
    }

    /**
     * 取图片列表第一张作为封面；无图返回 null
     */
    private String firstImage(java.util.List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0);
    }

    /**
     * 记录商品价格（价格表）
     */
    private void recordPrice(Long productId, String priceName, java.math.BigDecimal price) {
        ProductPrice priceRecord = new ProductPrice();
        priceRecord.setProductId(productId);
        priceRecord.setPriceName(priceName);
        priceRecord.setPrice(price);
        priceRecord.setCreateTime(LocalDateTime.now());
        productPriceMapper.insert(priceRecord);
    }

    /**
     * 批量填充商品分类名称
     */
    private void fillCategoryName(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        List<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        products.forEach(product ->
                product.setCategoryName(nameMap.getOrDefault(product.getCategoryId(), "未分类"))
        );
    }

    /**
     * 批量填充品牌名称（无品牌的商品 brandName 保持 null）
     */
    private void fillBrandName(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        List<Long> brandIds = products.stream()
                .map(Product::getBrandId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (brandIds.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = brandMapper.selectBatchIds(brandIds).stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName));
        products.forEach(product ->
                product.setBrandName(nameMap.get(product.getBrandId()))
        );
    }

    /**
     * 批量填充卖家昵称，便于前端展示
     */
    private void fillSellerName(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        List<Long> sellerIds = products.stream().map(Product::getSellerId).distinct().toList();
        List<User> sellers = userMapper.selectBatchIds(sellerIds);
        Map<Long, String> nameMap = sellers.stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
        products.forEach(product ->
                product.setSellerName(nameMap.getOrDefault(product.getSellerId(), "未知卖家"))
        );
    }
}
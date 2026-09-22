package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.AuthContext;
import com.shop.entity.ProductComment;
import com.shop.entity.User;
import com.shop.mapper.ProductCommentMapper;
import com.shop.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品评论服务
 */
@Service
@RequiredArgsConstructor
public class ProductCommentService {

    private final ProductCommentMapper productCommentMapper;

    private final UserMapper userMapper;

    /**
     * 查询某商品的全部评论（按时间正序），附带评论人昵称
     */
    public List<ProductComment> listByProduct(Long productId) {
        List<ProductComment> comments = productCommentMapper.selectList(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getProductId, productId)
                        .orderByAsc(ProductComment::getCreateTime)
        );
        if (comments == null || comments.isEmpty()) {
            return comments;
        }
        List<Long> userIds = comments.stream().map(ProductComment::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        comments.forEach(comment -> {
            User user = userMap.get(comment.getUserId());
            comment.setNickname(user == null ? "未知用户" : user.getNickname());
            comment.setAvatar(user == null ? null : user.getAvatar());
        });
        return comments;
    }

    /**
     * 发表评论（需登录）
     */
    public ProductComment create(Long productId, String content) {
        ProductComment comment = new ProductComment();
        comment.setProductId(productId);
        comment.setUserId(AuthContext.getUserId());
        comment.setContent(content.trim());
        comment.setCreateTime(LocalDateTime.now());
        productCommentMapper.insert(comment);
        return comment;
    }
}

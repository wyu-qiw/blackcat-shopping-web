package com.shop.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.AuthContext;
import com.shop.common.JwtUtil;
import com.shop.common.LoginUser;
import com.shop.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 登录与权限拦截器：
 * 1. 未登录用户不能访问受保护接口；
 * 2. /api/admin/** 仅平台管理员可访问；
 * 3. 前台商品浏览接口允许匿名访问，若携带令牌则自动填充登录上下文。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        LoginUser loginUser = token == null ? null : jwtUtil.parseToken(token);
        String uri = request.getRequestURI();
        boolean adminApi = uri.startsWith("/api/admin/");
        boolean protectedApi = adminApi || isProtectedApi(request);

        if (protectedApi && loginUser == null) {
            writeJson(response, Result.CODE_UNAUTHORIZED, "请先登录");
            return false;
        }
        if (adminApi && !"ADMIN".equals(loginUser.getRole())) {
            writeJson(response, Result.CODE_FORBIDDEN, "无权限访问后台管理接口");
            return false;
        }
        if (loginUser != null) {
            AuthContext.set(loginUser);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    /**
     * 判断接口是否需要登录（管理员接口由前缀统一拦截）
     */
    private boolean isProtectedApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod().toUpperCase();
        if ("/api/auth/me".equals(uri)) {
            return true;
        }
        if ("/api/products/mine".equals(uri) || "/api/orders/mine".equals(uri)
                || "/api/users/profile/limit".equals(uri)) {
            return true;
        }
        if ("/api/products".equals(uri) && "POST".equals(method)) {
            return true;
        }
        if ("/api/orders".equals(uri) && "POST".equals(method)) {
            return true;
        }
        // 购物车全部接口均需登录
        if (uri.startsWith("/api/cart")) {
            return true;
        }
        if ("/api/files/upload".equals(uri) && "POST".equals(method)) {
            return true;
        }
        // 商品编辑 / 上下架 / 删除 / 发表评论：售卖者或管理员操作，均需登录
        if (uri.startsWith("/api/products/")
                && ("PUT".equals(method) || "POST".equals(method) || "DELETE".equals(method))) {
            return true;
        }
        // 订单取消 / 申请退款
        if (uri.startsWith("/api/orders/") && "PUT".equals(method)) {
            return true;
        }
        // 个人中心修改信息 / 密码
        if (uri.startsWith("/api/users/") && "PUT".equals(method)) {
            return true;
        }
        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, msg)));
    }
}

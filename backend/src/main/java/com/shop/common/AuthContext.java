package com.shop.common;

/**
 * 登录用户上下文：拦截器解析 JWT 后写入，请求结束后清除
 */
public class AuthContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser loginUser = HOLDER.get();
        return loginUser == null ? null : loginUser.getId();
    }

    public static String getRole() {
        LoginUser loginUser = HOLDER.get();
        return loginUser == null ? null : loginUser.getRole();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public static void clear() {
        HOLDER.remove();
    }
}


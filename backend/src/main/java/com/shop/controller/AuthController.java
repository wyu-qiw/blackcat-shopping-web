package com.shop.controller;

import com.shop.common.Result;
import com.shop.dto.LoginRequest;
import com.shop.dto.RegisterRequest;
import com.shop.entity.User;
import com.shop.service.AuthService;
import com.shop.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册 / 登录 / 当前用户
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 普通用户注册（管理员不开放注册）
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody @Valid RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    /**
     * 账号密码登录，自动区分用户 / 管理员身份
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<User> me() {
        return Result.success(authService.getCurrentUser());
    }
}


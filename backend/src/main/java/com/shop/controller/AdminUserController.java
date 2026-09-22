package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.User;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员用户接口：全平台用户列表
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public Result<List<User>> listAll() {
        return Result.success(userService.listAll());
    }
}


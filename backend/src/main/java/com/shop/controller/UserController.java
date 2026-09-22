package com.shop.controller;

import com.shop.common.Result;
import com.shop.dto.PasswordChangeRequest;
import com.shop.dto.ProfileUpdateRequest;
import com.shop.entity.User;
import com.shop.service.UserService;
import com.shop.vo.ProfileLimitVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心接口：修改个人信息、修改密码（每月限额）、额度查询
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 修改个人信息（每月限额）
     */
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody @Valid ProfileUpdateRequest request) {
        return Result.success("个人信息修改成功", userService.updateProfile(request));
    }

    /**
     * 修改账号密码（每月限额）
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordChangeRequest request) {
        userService.changePassword(request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 当月修改额度
     */
    @GetMapping("/profile/limit")
    public Result<ProfileLimitVO> profileLimit() {
        return Result.success(userService.getProfileLimit());
    }
}

package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.AuthContext;
import com.shop.common.BizException;
import com.shop.common.JwtUtil;
import com.shop.dto.LoginRequest;
import com.shop.dto.RegisterRequest;
import com.shop.entity.User;
import com.shop.enums.RoleEnum;
import com.shop.mapper.UserMapper;
import com.shop.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 认证服务：注册、登录、获取当前用户
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    /**
     * 普通用户注册：默认角色 USER，注册成功后直接返回令牌
     */
    public LoginVO register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count != null && count > 0) {
            throw new BizException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setRole(RoleEnum.USER.name());
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, user);
    }

    /**
     * 登录：校验用户名密码，返回令牌与用户信息
     */
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, user);
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Long userId = AuthContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "登录状态已失效，请重新登录");
        }
        return user;
    }
}


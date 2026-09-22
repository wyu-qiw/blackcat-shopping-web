package com.shop.vo;

import com.shop.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录返回数据：令牌 + 用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;

    private User user;
}


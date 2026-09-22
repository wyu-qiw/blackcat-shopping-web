package com.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人信息请求参数
 */
@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长50个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;
}

package com.shop.controller;

import com.shop.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 根路径提示：浏览器直接访问后端地址时给出友好说明，避免误认为后端报错
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public Result<Void> index() {
        return Result.success("线上购物平台后端服务运行正常，前端请访问 http://localhost:5173", null);
    }
}

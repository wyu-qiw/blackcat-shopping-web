package com.shop.controller;

import com.shop.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 页面与 API 基础路径入口：
 * 1. 访问后端根路径或前端路由时，返回 Spring Boot 托管的 Vue 页面；
 * 2. 访问 /api 时返回后端服务状态，方便 Apifox 检查服务是否在线。
 */
@Controller
public class IndexController {

    @GetMapping({"/", "/login", "/register", "/buy", "/publish"})
    public String appPage() {
        return "forward:/index.html";
    }

    @GetMapping({"/products/{id}", "/products/{id}/comments"})
    public String productPage() {
        return "forward:/index.html";
    }

    @GetMapping({"/my", "/my/**"})
    public String userCenterPage() {
        return "forward:/index.html";
    }

    @GetMapping({"/admin", "/admin/**"})
    public String adminPage() {
        return "forward:/index.html";
    }

    @GetMapping("/favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void favicon() {
        // 浏览器自动请求；没有独立图标时不返回 404 JSON
    }

    @GetMapping({"/api", "/api/"})
    @ResponseBody
    public ResponseEntity<Result<Map<String, Object>>> apiIndex() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("frontendUrl", "http://localhost:5173");
        data.put("backendPageUrl", "http://localhost:8080");
        data.put("apiBaseUrl", "http://localhost:8080/api");
        data.put("exampleEndpoints", new String[]{
                "/api/auth/login",
                "/api/products",
                "/api/categories",
                "/api/brands",
                "/api/cart",
                "/api/admin/stats"
        });

        return ResponseEntity.ok(Result.success("线上购物平台后端服务运行正常", data));
    }
}


package com.shop.config;

import com.shop.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Web 配置：跨域、权限拦截器、本地图片静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final UploadPathProvider uploadPathProvider;

    public WebConfig(AuthInterceptor authInterceptor, UploadPathProvider uploadPathProvider) {
        this.authInterceptor = authInterceptor;
        this.uploadPathProvider = uploadPathProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = uploadPathProvider.getUploadDir().toAbsolutePath().normalize();
        String path = dir.toString().replace("\\", "/");
        if (!path.endsWith("/")) {
            path += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + path);
    }
}

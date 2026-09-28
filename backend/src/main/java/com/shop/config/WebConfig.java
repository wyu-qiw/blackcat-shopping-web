package com.shop.config;

import com.shop.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Web 配置：跨域、权限拦截器、上传图片与前端构建产物静态资源映射。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final UploadPathProvider uploadPathProvider;
    private final FrontendDistProvider frontendDistProvider;

    public WebConfig(AuthInterceptor authInterceptor,
                     UploadPathProvider uploadPathProvider,
                     FrontendDistProvider frontendDistProvider) {
        this.authInterceptor = authInterceptor;
        this.uploadPathProvider = uploadPathProvider;
        this.frontendDistProvider = frontendDistProvider;
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
        Path uploadDir = uploadPathProvider.getUploadDir().toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir.toUri().toString());

        if (frontendDistProvider.isAvailable()) {
            Path distDir = frontendDistProvider.getDistDir();
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations(distDir.resolve("assets").toUri().toString());
            registry.addResourceHandler("/index.html", "/favicon.png")
                    .addResourceLocations(distDir.toUri().toString());
        }
    }
}

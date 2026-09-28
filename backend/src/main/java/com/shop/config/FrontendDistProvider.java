package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 前端构建产物目录解析器。
 * 本地开发时 Spring Boot 直接托管 ../frontend/dist，
 * 使后端地址也能打开与前端完全一致的页面。
 */
@Component
public class FrontendDistProvider {

    private final Path distDir;

    public FrontendDistProvider(@Value("${app.frontend-dist:../frontend/dist}") String configured) {
        this.distDir = resolve(configured);
    }

    public Path getDistDir() {
        return distDir;
    }

    public boolean isAvailable() {
        return Files.exists(distDir.resolve("index.html"));
    }

    private Path resolve(String configured) {
        Path cfgPath = Paths.get(configured);
        if (cfgPath.isAbsolute()) {
            return cfgPath.normalize().toAbsolutePath();
        }

        Path backendDir = resolveBackendDir();
        if (backendDir != null) {
            Path candidate = backendDir.resolve(configedSafe(configured)).normalize().toAbsolutePath();
            if (Files.exists(candidate.resolve("index.html"))) {
                return candidate;
            }
        }

        return Paths.get(configured).toAbsolutePath().normalize();
    }

    private String configedSafe(String configured) {
        return configured.replace('/', java.io.File.separatorChar);
    }

    private Path resolveBackendDir() {
        try {
            URI location = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Path codeSource = Paths.get(location);

            if (Files.isRegularFile(codeSource)) {
                Path targetDir = codeSource.getParent();
                if (targetDir != null && targetDir.getParent() != null) {
                    return targetDir.getParent();
                }
            }

            Path targetDir = codeSource.getParent();
            if (targetDir != null && targetDir.getParent() != null) {
                return targetDir.getParent();
            }
        } catch (Exception ignored) {
            // 回退到工作目录
        }
        return null;
    }
}

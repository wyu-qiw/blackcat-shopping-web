package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 上传目录解析器。
 * 配置的相对路径会基于 jar/classes 所在的项目根目录解析，而不是进程工作目录，
 * 避免从不同目录启动后端时 uploads 映射失效导致图片 404。
 */
@Component
public class UploadPathProvider {

    private final Path uploadDir;

    public UploadPathProvider(@Value("${app.upload-dir:uploads}") String configured) {
        this.uploadDir = resolve(configured);
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    private Path resolve(String configured) {
        Path cfg = Paths.get(configured);
        if (cfg.isAbsolute()) {
            return cfg.normalize();
        }
        Path base = resolveBaseDir();
        if (base != null) {
            return base.resolve(configured).normalize().toAbsolutePath();
        }
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    private Path resolveBaseDir() {
        try {
            URI location = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Path codeSource = Paths.get(location);
            Path target = codeSource.getParent();
            if (target != null && target.getParent() != null) {
                return target.getParent();
            }
        } catch (Exception ignored) {
            // 解析失败时回退到进程工作目录
        }
        return null;
    }
}

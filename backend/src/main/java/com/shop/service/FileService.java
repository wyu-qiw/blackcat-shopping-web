package com.shop.service;

import com.shop.common.BizException;
import com.shop.config.UploadPathProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * 图片上传服务：保存到本地 uploads 目录并返回访问地址
 */
@Service
public class FileService {

    private static final List<String> ALLOWED_EXT = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final UploadPathProvider uploadPathProvider;

    public FileService(UploadPathProvider uploadPathProvider) {
        this.uploadPathProvider = uploadPathProvider;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        String originalName = file.getOriginalFilename();
        int index = originalName == null ? -1 : originalName.lastIndexOf('.');
        String ext = index < 0 ? "" : originalName.substring(index).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持 jpg/jpeg/png/gif/webp 格式图片");
        }

        try {
            Path dir = uploadPathProvider.getUploadDir().toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            file.transferTo(dir.resolve(filename).toFile());
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new BizException(500, "图片上传失败，请稍后重试");
        }
    }
}

package org.youxx.common.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.youxx.common.service.FileStorageService;
import org.youxx.common.util.ImageUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    // 静态资源根目录(仓库根)，与 WebMvcConfig 的 base-dir 同源，避免依赖 user.dir
    @Value("${youxx.resource.base-dir}")
    private String resourceBaseDir;

    @Override
    public String storeImage(MultipartFile file, String subDir, int maxEdge) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        if (!ImageUtils.isImage(file)) {
            throw new IllegalArgumentException("仅支持 jpg/png/gif 等图片");
        }

        // 压缩为 JPEG 字节，统一输出 .jpg 扩展名
        byte[] compressed = ImageUtils.compress(file, maxEdge, 0.8f);

        String filename = UUID.randomUUID() + ".jpg";
        // subDir 允许是多级目录(如 "products/drinks")：统一用 "/" 规范化，
        // 落盘时由 Paths.get 解析平台分隔符，返回的对外 URL 始终用 "/" 分隔
        String normalizedSubDir = subDir.replace("\\", "/").replaceAll("/+", "/");
        if (normalizedSubDir.startsWith("/")) {
            normalizedSubDir = normalizedSubDir.substring(1);
        }
        if (normalizedSubDir.endsWith("/")) {
            normalizedSubDir = normalizedSubDir.substring(0, normalizedSubDir.length() - 1);
        }
        String uploadDir = resourceBaseDir + File.separator + "upload_resources" + File.separator
                + normalizedSubDir.replace("/", File.separator);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, compressed);

            log.info("图片已存储(压缩后 {} 字节, 最长边≤{}px): {}", compressed.length, maxEdge, filePath);
            return "/upload_resources/" + normalizedSubDir + "/" + filename;
        } catch (Exception e) {
            log.error("图片存储失败", e);
            throw new RuntimeException("图片存储失败: " + e.getMessage(), e);
        }
    }
}

package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AvatarService {
    public static final String DEFAULT_AVATAR_URL = "/avatar/default.jpg";
    private static final String AVATAR_DIR = "avatar";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.upload.file-dir}")
    private String fileDir;

    public String uploadAvatar(MultipartFile file) {
        // 1. 校验
        if (file.isEmpty()) {
            throw new BusinessException("Upload file is empty");
        }
        // 自动创建目录（包括父目录）
        Path avatarDir = Paths.get(fileDir, AVATAR_DIR);
        try {
            Files.createDirectories(avatarDir);
        } catch (IOException e) {
            throw new BusinessException("File dir not exists, please contact admin");
        }

        // 2. 生成安全文件名（避免覆盖和路径穿越）
        String originalName = file.getOriginalFilename();
        String ext = "";
        //noinspection ConstantValue
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("Unsupported file type");
        }
        String safeFileName = UUID.randomUUID() + ext;

        // 3. 保存文件
        try {
            Path targetPath = avatarDir.resolve(safeFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Upload file error, please contact admin");
        }

        // 4. 返回可访问的 URL 路径
        return "/" + AVATAR_DIR + "/" + safeFileName;
    }

    @Async
    public void deleteAvatar(String icon) {
        if (!StringUtils.hasText(icon))
            return;
        if (!icon.startsWith("/avatar/"))
            return;
        try {
            // 跳过默认头像
            if (DEFAULT_AVATAR_URL.equals(icon))
                return;

            // 自动创建目录
            Path baseDir = Paths.get(fileDir);
            try {
                Files.createDirectories(baseDir);
            } catch (IOException e) {
                throw new BusinessException("File dir not exists, please contact admin");
            }
            // 删除旧文件
            try {
                Path targetPath = baseDir.resolve(icon);
                Files.deleteIfExists(targetPath);
            } catch (IOException e) {
                throw new BusinessException("Delete old file error, please contact admin");
            }
        } catch (Exception e) {
            log.warn("Failed to delete avatar {}: {}", icon, e.getMessage());
        }
    }

    @Async
    public CompletableFuture<String> saveAvatarToLocal(String githubId, String githubAvatarUrl) {
        try {
            // 自动创建目录（包括父目录）
            Path avatarDir = Paths.get(fileDir, AVATAR_DIR);
            Files.createDirectories(avatarDir);

            // 下载图片
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "QingxinTutor/2.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Resource> response = restTemplate.exchange(
                    githubAvatarUrl,
                    HttpMethod.GET,
                    entity,
                    Resource.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                try (InputStream inputStream = response.getBody().getInputStream()) {
                    String safeFileName = UUID.randomUUID() + ".png";
                    Path targetPath = avatarDir.resolve(safeFileName);
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    return CompletableFuture.completedFuture("/" + AVATAR_DIR + "/" + safeFileName);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to download avatar for GitHub ID {}: {}", githubId, e.getMessage());
        }

        return CompletableFuture.completedFuture(DEFAULT_AVATAR_URL);
    }
}

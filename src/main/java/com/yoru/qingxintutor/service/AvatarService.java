package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AvatarService {

    @Value("${app.upload.file-dir}")
    private String fileDir;

    private static final String AVATAR_DIR = "avatar";

    @SuppressWarnings("ExtractMethodRecommender")
    public String uploadAvatar(MultipartFile file) {
        // 1. 校验
        if (file.isEmpty()) {
            throw new BusinessException("Upload file is empty");
        }
        File dir = new File(fileDir, AVATAR_DIR);
        if (!dir.exists()) {
            throw new BusinessException("File dir not exists, please contact admin");
        }

        // 2. 生成安全文件名（避免覆盖和路径穿越）
        String originalName = file.getOriginalFilename();
        String ext = "";
        //noinspection ConstantValue
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String safeFileName = UUID.randomUUID() + ext;

        // 3. 保存文件
        try {
            Path targetPath = Paths.get(fileDir, AVATAR_DIR, safeFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Upload file error, please contact admin");
        }

        // 4. 返回可访问的 URL 路径
        return "/" + AVATAR_DIR + "/" + safeFileName;
    }
}

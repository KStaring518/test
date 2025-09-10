package com.example.shop.controller;

import com.example.shop.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 上传商品图片
     */
    @PostMapping("/product-image")
public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file, 
                                       @RequestParam(value = "productId", required = false) Long productId) {
    try {
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        // 检查文件大小 (限制为5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }

        // 创建上传目录 - 修复：确保目录存在
        String productImagePath = uploadPath + "/products";
        Path uploadDir = Paths.get(productImagePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名 - 修复：使用UUID避免冲突
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 使用UUID生成唯一文件名，避免重复
        String filename = "product_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;
        Path filePath = uploadDir.resolve(filename);
        
        // 保存文件
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回相对路径 - 修复：确保返回正确的相对路径
        String imageUrl = urlPrefix + "/products/" + filename;
        
        System.out.println("=== 商品图片上传调试 ===");
        System.out.println("上传目录: " + uploadDir.toAbsolutePath());
        System.out.println("文件名: " + filename);
        System.out.println("返回URL: " + imageUrl);
        System.out.println("文件是否存在: " + Files.exists(filePath));
        System.out.println("=========================");
        
        return Result.success(imageUrl);
    } catch (Exception e) {
        e.printStackTrace();
        return Result.error("图片上传失败: " + e.getMessage());
    }
}

    /**
     * 通用图片上传（用于后台轮播图等）
     * 默认保存到 uploads/banners 目录，前端也可通过 dir 指定子目录
     */
    @PostMapping("/image")
    public Result<String> uploadCommonImage(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "dir", required = false, defaultValue = "banners") String dir) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 最大 5MB
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("图片大小不能超过5MB");
            }

            // 目标目录
            String targetDirPath = uploadPath + "/" + dir;
            Path targetDir = Paths.get(targetDirPath);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "image_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;
            Path filePath = targetDir.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = urlPrefix + "/" + dir + "/" + filename;

            System.out.println("=== 通用图片上传调试 ===");
            System.out.println("上传目录: " + targetDir.toAbsolutePath());
            System.out.println("文件名: " + filename);
            System.out.println("返回URL: " + imageUrl);
            System.out.println("文件是否存在: " + Files.exists(filePath));
            System.out.println("=========================");

            return Result.success(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
    try {
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        // 检查文件大小 (限制为2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("头像大小不能超过2MB");
        }

        // 创建上传目录
        String avatarPath = uploadPath + "/avatars";
        Path uploadDir = Paths.get(avatarPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "avatar_" + UUID.randomUUID().toString().replace("-", "") + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        // 返回访问URL - 保持原来的逻辑
        String imageUrl = urlPrefix + "/avatars/" + filename;
        
        System.out.println("=== 头像上传调试 ===");
        System.out.println("上传目录: " + uploadDir.toAbsolutePath());
        System.out.println("文件名: " + filename);
        System.out.println("返回URL: " + imageUrl);
        System.out.println("文件是否存在: " + Files.exists(filePath));
        System.out.println("=========================");
        
        return Result.success(imageUrl);

    } catch (IOException e) {
        e.printStackTrace();
        return Result.error("头像上传失败: " + e.getMessage());
    }
}

    /**
     * 删除图片文件
     */
    @DeleteMapping("/image")
    public Result<Void> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        try {
            if (imageUrl.startsWith(urlPrefix)) {
                String relativePath = imageUrl.substring(urlPrefix.length());
                Path filePath = Paths.get(uploadPath + relativePath);
                
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    return Result.success();
                }
            }
            return Result.error("图片文件不存在");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("删除图片失败: " + e.getMessage());
        }
    }
}

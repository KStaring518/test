package com.example.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 配置上传文件的静态资源访问
    String absolutePath = new File(uploadPath).getAbsolutePath();
    System.out.println("=== WebConfig 调试信息 ===");
    System.out.println("上传路径: " + uploadPath);
    System.out.println("绝对路径: " + absolutePath);
    System.out.println("=========================");
    
    // 修复：确保路径分隔符正确，使用File.separator
    String normalizedPath = absolutePath.replace("\\", "/");
    if (!normalizedPath.endsWith("/")) {
        normalizedPath += "/";
    }
    
    System.out.println("规范化路径: " + normalizedPath);
    
    // 生成 file URI（Windows 形如 file:/C:/.../uploads/）
    String fileUri = new File(normalizedPath).toURI().toString();
    System.out.println("资源URI: " + fileUri);

    // 修复：使用正确的资源位置格式
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations(fileUri)
            .setCachePeriod(3600) // 缓存1小时
            .resourceChain(true);
}
}

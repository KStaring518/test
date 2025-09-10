package com.example.shop.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TestController {
    
    @GetMapping("/public")
    public String publicEndpoint() {
        return "这是一个公开的测试接口";
    }
    
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "这是一个受保护的测试接口";
    }
}

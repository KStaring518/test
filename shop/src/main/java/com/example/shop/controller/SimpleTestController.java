package com.example.shop.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SimpleTestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello from SimpleTestController!";
    }
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}

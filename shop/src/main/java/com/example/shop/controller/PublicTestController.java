package com.example.shop.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PublicTestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello from PublicTestController!";
    }
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @PostMapping("/echo")
    public String echo(@RequestBody String message) {
        return "Echo: " + message;
    }
}

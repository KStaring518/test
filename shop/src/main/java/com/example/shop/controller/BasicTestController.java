package com.example.shop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BasicTestController {
    
    @GetMapping("/basic-test")
    public String basicTest() {
        return "Basic test controller is working!";
    }
}

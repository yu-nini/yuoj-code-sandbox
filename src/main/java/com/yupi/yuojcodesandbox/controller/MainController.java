package com.yupi.yuojcodesandbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class MainController {
    @GetMapping("/test")
    public String healthCheck(){
        return "ok";
    }
}

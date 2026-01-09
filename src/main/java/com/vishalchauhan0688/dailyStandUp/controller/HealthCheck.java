package com.vishalchauhan0688.dailyStandUp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/health")
public class HealthCheck {

    @GetMapping
    public boolean check(){
        return true;
    }
}

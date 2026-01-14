package com.ihome24.ihome24.controller.publicapi.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/publicapi/auth")
public class AuthController {

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> loginInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Для авторизации отправьте POST запрос на /api/auth/login с параметрами username и password");
        response.put("username", "admin");
        response.put("password", "admin");
        response.put("loginUrl", "/api/auth/login");
        return ResponseEntity.ok(response);
    }
}

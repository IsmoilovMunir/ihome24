package com.ihome24.ihome24.controller.publicapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Добро пожаловать в iHome24 API");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
                "login", "/api/auth/login",
                "adminProducts", "/api/admin/products",
                "authInfo", "/api/publicapi/auth/info"
        ));
        return ResponseEntity.ok(response);
    }
}

package com.ihome24.ihome24.controller.publicapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/geo")
@CrossOrigin(origins = "*")
public class GeoLocationRestController {

    private static final String IP_API_URL = "http://ip-api.com/json/?lang=ru&fields=status,city,regionName";

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/location")
    public ResponseEntity<Map<String, String>> getLocation() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(IP_API_URL, Map.class);
            if (response != null && "success".equals(response.get("status")) && response.get("city") != null) {
                String city = response.get("city").toString();
                return ResponseEntity.ok(Map.of("city", city));
            }
        } catch (Exception ignored) {
            // Fallback при ошибке
        }
        return ResponseEntity.ok(Map.of("city", "Москва"));
    }
}

package com.ihome24.ihome24.controller.publicapi.settings;

import com.ihome24.ihome24.dto.response.company.PriceTiersSettingsResponse;
import com.ihome24.ihome24.service.company.CompanySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsRestController {

    private final CompanySettingsService companySettingsService;

    /**
     * Публичный endpoint для получения уровней цен (розница / мелкий опт / крупный опт).
     * Используется фронтендом для расчёта цены в зависимости от количества.
     */
    @GetMapping("/price-tiers")
    public ResponseEntity<PriceTiersSettingsResponse> getPriceTiers() {
        return ResponseEntity.ok(companySettingsService.getPriceTiers());
    }
}

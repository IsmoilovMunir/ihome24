package com.ihome24.ihome24.controller.admin.company;

import com.ihome24.ihome24.dto.request.company.CompanySettingsRequest;
import com.ihome24.ihome24.dto.response.company.CompanySettingsResponse;
import com.ihome24.ihome24.service.company.CompanySettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin/company-settings", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminCompanySettingsRestController {

    private final CompanySettingsService companySettingsService;

    @GetMapping
    public ResponseEntity<CompanySettingsResponse> getCompanySettings() {
        CompanySettingsResponse settings = companySettingsService.getCompanySettings();
        if (settings == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(settings);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<CompanySettingsResponse> createOrUpdateCompanySettings(
            @Valid @RequestBody CompanySettingsRequest request) {
        CompanySettingsResponse settings = companySettingsService.createOrUpdateCompanySettings(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settings);
    }

    @PutMapping(consumes = "application/json")
    public ResponseEntity<CompanySettingsResponse> updateCompanySettings(
            @Valid @RequestBody CompanySettingsRequest request) {
        CompanySettingsResponse settings = companySettingsService.createOrUpdateCompanySettings(request);
        return ResponseEntity.ok(settings);
    }
}

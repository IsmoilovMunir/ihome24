package com.ihome24.ihome24.controller.publicapi.wholesale;

import com.ihome24.ihome24.dto.request.wholesale.WholesaleLeadRequest;
import com.ihome24.ihome24.service.wholesale.WholesaleLeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/publicapi/wholesale")
@RequiredArgsConstructor
@Slf4j
public class WholesaleRestController {

    private final WholesaleLeadService wholesaleLeadService;

    @PostMapping("/lead")
    public ResponseEntity<?> submitLead(@Valid @RequestBody WholesaleLeadRequest request) {
        try {
            WholesaleLeadService.Result result = wholesaleLeadService.submitLead(request);
            return ResponseEntity.ok(Map.of(
                    "success", result.success(),
                    "emailSent", result.emailSent(),
                    "telegramSent", result.telegramSent()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

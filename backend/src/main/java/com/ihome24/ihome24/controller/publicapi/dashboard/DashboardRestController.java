package com.ihome24.ihome24.controller.publicapi.dashboard;

import com.ihome24.ihome24.dto.response.dashboard.DashboardFullResponse;
import com.ihome24.ihome24.dto.response.dashboard.DashboardStatsResponse;
import com.ihome24.ihome24.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apps/ecommerce/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardRestController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/full")
    public ResponseEntity<DashboardFullResponse> getFullDashboard() {
        return ResponseEntity.ok(dashboardService.getFullDashboard());
    }
}

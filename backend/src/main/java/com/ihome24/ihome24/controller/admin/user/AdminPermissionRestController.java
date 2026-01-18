package com.ihome24.ihome24.controller.admin.user;

import com.ihome24.ihome24.dto.response.user.PermissionListResponse;
import com.ihome24.ihome24.service.user.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apps/permissions")
@RequiredArgsConstructor
public class AdminPermissionRestController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<PermissionListResponse> getPermissions(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer itemsPerPage,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String orderBy
    ) {
        PermissionListResponse response = permissionService.getPermissions(q, page, itemsPerPage, sortBy, orderBy);
        return ResponseEntity.ok(response);
    }
}

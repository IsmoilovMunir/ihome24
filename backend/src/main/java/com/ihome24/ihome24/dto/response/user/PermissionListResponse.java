package com.ihome24.ihome24.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionListResponse {
    private List<PermissionResponse> permissions;
    private Long totalPermissions;
}

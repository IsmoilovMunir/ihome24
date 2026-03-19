package com.ihome24.ihome24.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeUserRoleRequest {
    @NotBlank(message = "Role name is required")
    private String roleName;
}


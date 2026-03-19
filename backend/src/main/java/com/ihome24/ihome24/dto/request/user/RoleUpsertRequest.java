package com.ihome24.ihome24.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpsertRequest {
    /**
     * Technical unique role name, e.g. admin, manager, users.
     */
    @NotBlank(message = "Role name is required")
    private String name;

    /**
     * Human readable name shown in UI.
     */
    private String displayName;

    /**
     * Permission names to assign to role.
     */
    private List<String> permissions;
}


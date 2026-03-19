package com.ihome24.ihome24.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCreateAdminUserRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    /**
     * Optional. If empty, server will generate username from email.
     */
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Role name is required")
    private String roleName;

    /**
     * Optional. If empty, server generates secure temporary password.
     */
    private String temporaryPassword;
}


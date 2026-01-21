package com.ihome24.ihome24.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "Username is required")
    private String username;

    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String avatar;
    private String company;
    private String country;
    private String contact;
    private String currentPlan;
    private String billing;

    @NotNull(message = "Role is required")
    private Long roleId;

    private String status; // ACTIVE, INACTIVE, PENDING
}

package com.ihome24.ihome24.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRegistrationRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
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

    @NotBlank(message = "Role name is required (admin or manager)")
    private String roleName; // "admin" or "manager"
}

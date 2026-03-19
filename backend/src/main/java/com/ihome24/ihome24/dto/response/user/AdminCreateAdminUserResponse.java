package com.ihome24.ihome24.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateAdminUserResponse {
    private UserResponse user;
    /**
     * Temporary password shown once to admin.
     */
    private String temporaryPassword;
    private Boolean emailSent;
    private String message;
}


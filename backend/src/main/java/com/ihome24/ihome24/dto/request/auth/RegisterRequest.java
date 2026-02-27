package com.ihome24.ihome24.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Номер телефона обязателен")
    private String phone;

    @NotBlank(message = "ФИО обязательно")
    private String fullName;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;

    private String email; // опционально
}

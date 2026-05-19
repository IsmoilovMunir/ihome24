package com.ihome24.ihome24.dto.request.wholesale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WholesaleLeadRequest {
    @NotBlank(message = "Укажите имя или компанию")
    @Size(max = 200, message = "Имя слишком длинное")
    private String name;

    @NotBlank(message = "Укажите телефон")
    @Size(max = 30, message = "Телефон слишком длинный")
    private String phone;

    @Size(max = 20, message = "ИНН слишком длинный")
    private String inn;

    @Size(max = 2000, message = "Комментарий слишком длинный")
    private String message;

    /** Honeypot — боты заполняют, люди нет */
    private String website;
}

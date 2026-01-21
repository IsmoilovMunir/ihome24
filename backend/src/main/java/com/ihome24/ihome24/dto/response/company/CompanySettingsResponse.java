package com.ihome24.ihome24.dto.response.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettingsResponse {
    private Long id;
    private String name;
    private String inn;
    private String ogrn;
    private String kpp;
    private String country;
    private String legalAddress;
    private String actualAddress;
    private String phone;
    private String email;
    private String website;
    private String directorName;
    private String accountantName;
    private String bankName;
    private String bankAccount;
    private String correspondentAccount;
    private String bik;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

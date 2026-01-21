package com.ihome24.ihome24.service.company;

import com.ihome24.ihome24.dto.request.company.CompanySettingsRequest;
import com.ihome24.ihome24.dto.response.company.CompanySettingsResponse;
import com.ihome24.ihome24.entity.company.CompanySettings;
import com.ihome24.ihome24.repository.company.CompanySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanySettingsService {

    private final CompanySettingsRepository companySettingsRepository;

    @Transactional(readOnly = true)
    public CompanySettingsResponse getCompanySettings() {
        CompanySettings settings = companySettingsRepository
                .findFirstByIsActiveTrue()
                .orElseGet(() -> companySettingsRepository.findFirstByOrderByIdAsc()
                        .orElse(null));

        if (settings == null) {
            return null;
        }

        return mapToResponse(settings);
    }

    @Transactional
    public CompanySettingsResponse createOrUpdateCompanySettings(CompanySettingsRequest request) {
        // Деактивируем все существующие настройки
        companySettingsRepository.findAll().forEach(settings -> {
            settings.setIsActive(false);
            companySettingsRepository.save(settings);
        });

        // Создаем новые активные настройки
        CompanySettings settings = CompanySettings.builder()
                .name(request.getName())
                .inn(request.getInn())
                .ogrn(request.getOgrn())
                .kpp(request.getKpp())
                .country(request.getCountry() != null ? request.getCountry() : "Russia")
                .legalAddress(request.getLegalAddress())
                .actualAddress(request.getActualAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .directorName(request.getDirectorName())
                .accountantName(request.getAccountantName())
                .bankName(request.getBankName())
                .bankAccount(request.getBankAccount())
                .correspondentAccount(request.getCorrespondentAccount())
                .bik(request.getBik())
                .isActive(true)
                .build();

        CompanySettings savedSettings = companySettingsRepository.save(settings);
        return mapToResponse(savedSettings);
    }

    private CompanySettingsResponse mapToResponse(CompanySettings settings) {
        return CompanySettingsResponse.builder()
                .id(settings.getId())
                .name(settings.getName())
                .inn(settings.getInn())
                .ogrn(settings.getOgrn())
                .kpp(settings.getKpp())
                .country(settings.getCountry())
                .legalAddress(settings.getLegalAddress())
                .actualAddress(settings.getActualAddress())
                .phone(settings.getPhone())
                .email(settings.getEmail())
                .website(settings.getWebsite())
                .directorName(settings.getDirectorName())
                .accountantName(settings.getAccountantName())
                .bankName(settings.getBankName())
                .bankAccount(settings.getBankAccount())
                .correspondentAccount(settings.getCorrespondentAccount())
                .bik(settings.getBik())
                .isActive(settings.getIsActive())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}

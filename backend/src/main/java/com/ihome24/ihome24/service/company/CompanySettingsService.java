package com.ihome24.ihome24.service.company;

import com.ihome24.ihome24.dto.request.company.CompanySettingsRequest;
import com.ihome24.ihome24.dto.request.company.CurrencySettingsRequest;
import com.ihome24.ihome24.dto.response.company.CompanySettingsResponse;
import com.ihome24.ihome24.entity.company.CompanySettings;
import com.ihome24.ihome24.repository.company.CompanySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        CompanySettings current = companySettingsRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> companySettingsRepository.findFirstByOrderByIdAsc().orElse(null));

        // Деактивируем все существующие настройки
        companySettingsRepository.findAll().forEach(settings -> {
            settings.setIsActive(false);
            companySettingsRepository.save(settings);
        });

        BigDecimal rate = request.getCurrencyRate() != null ? request.getCurrencyRate()
                : (current != null && current.getCurrencyRate() != null ? current.getCurrencyRate() : BigDecimal.ONE);
        BigDecimal percent = request.getCurrencyPercentAdjustment() != null ? request.getCurrencyPercentAdjustment()
                : (current != null && current.getCurrencyPercentAdjustment() != null ? current.getCurrencyPercentAdjustment() : BigDecimal.ZERO);

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
                .currencyRate(rate)
                .currencyPercentAdjustment(percent)
                .isActive(true)
                .build();

        CompanySettings savedSettings = companySettingsRepository.save(settings);
        return mapToResponse(savedSettings);
    }

    @Transactional
    public CompanySettingsResponse updateCurrencySettings(CurrencySettingsRequest request) {
        CompanySettings settings = companySettingsRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> companySettingsRepository.findFirstByOrderByIdAsc().orElse(null));

        // Если настроек ещё нет совсем — создаём запись по умолчанию,
        // чтобы можно было задать только валюту и процент.
        if (settings == null) {
            settings = CompanySettings.builder()
                    .name("Мой магазин")
                    .country("Russia")
                    .isActive(true)
                    .build();
        }

        settings.setCurrencyRate(request.getCurrencyRate());
        settings.setCurrencyPercentAdjustment(request.getCurrencyPercentAdjustment());
        settings.setIsActive(true);
        settings = companySettingsRepository.save(settings);
        return mapToResponse(settings);
    }

    /**
     * Возвращает настройки валюты, при необходимости создавая запись с
     * значениями по умолчанию (курс = 1, процент = 0).
     */
    @Transactional
    public CompanySettingsResponse getOrCreateCurrencySettings() {
        CompanySettings settings = companySettingsRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> companySettingsRepository.findFirstByOrderByIdAsc().orElse(null));

        if (settings == null) {
            settings = CompanySettings.builder()
                    .name("Мой магазин")
                    .country("Russia")
                    .currencyRate(BigDecimal.ONE)
                    .currencyPercentAdjustment(BigDecimal.ZERO)
                    .isActive(true)
                    .build();
        } else {
            if (settings.getCurrencyRate() == null) {
                settings.setCurrencyRate(BigDecimal.ONE);
            }
            if (settings.getCurrencyPercentAdjustment() == null) {
                settings.setCurrencyPercentAdjustment(BigDecimal.ZERO);
            }
            if (settings.getIsActive() == null || !settings.getIsActive()) {
                settings.setIsActive(true);
            }
        }

        settings = companySettingsRepository.save(settings);
        return mapToResponse(settings);
    }

    /**
     * Вычисляет отображаемую цену: базовая цена × курс × (1 + процент коррекции / 100).
     * Используется для каталога, корзины и заказов.
     */
    @Transactional(readOnly = true)
    public BigDecimal getDisplayPrice(BigDecimal basePrice) {
        if (basePrice == null) return BigDecimal.ZERO;
        CompanySettings settings = companySettingsRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> companySettingsRepository.findFirstByOrderByIdAsc().orElse(null));
        BigDecimal rate = settings != null && settings.getCurrencyRate() != null ? settings.getCurrencyRate() : BigDecimal.ONE;
        BigDecimal percent = settings != null && settings.getCurrencyPercentAdjustment() != null ? settings.getCurrencyPercentAdjustment() : BigDecimal.ZERO;
        return basePrice.multiply(rate).multiply(BigDecimal.ONE.add(percent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))).setScale(2, RoundingMode.HALF_UP);
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
                .currencyRate(settings.getCurrencyRate())
                .currencyPercentAdjustment(settings.getCurrencyPercentAdjustment())
                .isActive(settings.getIsActive())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}

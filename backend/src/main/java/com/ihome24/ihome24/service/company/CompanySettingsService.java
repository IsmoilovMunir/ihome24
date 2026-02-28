package com.ihome24.ihome24.service.company;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome24.ihome24.dto.request.company.CompanySettingsRequest;
import com.ihome24.ihome24.dto.request.company.CurrencySettingsRequest;
import com.ihome24.ihome24.dto.request.company.PriceTierItem;
import com.ihome24.ihome24.dto.request.company.PriceTiersSettingsRequest;
import com.ihome24.ihome24.dto.response.company.CompanySettingsResponse;
import com.ihome24.ihome24.dto.response.company.PriceTiersSettingsResponse;
import com.ihome24.ihome24.entity.company.CompanySettings;
import com.ihome24.ihome24.repository.company.CompanySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanySettingsService {

    private static final TypeReference<List<PriceTierItem>> PRICE_TIERS_TYPE = new TypeReference<>() {};

    private final CompanySettingsRepository companySettingsRepository;
    private final ObjectMapper objectMapper;

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
                .priceTiersJson(current != null ? current.getPriceTiersJson() : null)
                .isActive(true)
                .build();

        CompanySettings savedSettings = companySettingsRepository.save(settings);
        return mapToResponse(savedSettings);
    }

    @Transactional
    public CompanySettingsResponse updateCurrencySettings(CurrencySettingsRequest request) {
        CompanySettings settings = companySettingsRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> companySettingsRepository.findFirstByOrderByIdAsc().orElse(null));

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

    /**
     * Цена за единицу с учётом объёмной скидки: сначала курс валюты, затем скидка по уровню (розница/мелкий опт/крупный опт).
     * Если уровни не настроены — возвращается цена только с курсом (без скидки по количеству).
     */
    @Transactional(readOnly = true)
    public BigDecimal getUnitPriceForQuantity(BigDecimal basePrice, int quantity) {
        if (basePrice == null) return BigDecimal.ZERO;
        BigDecimal displayPrice = getDisplayPrice(basePrice);
        List<PriceTierItem> tiers = getPriceTiersList();
        if (tiers == null || tiers.isEmpty()) {
            tiers = getDefaultPriceTiers();
        }
        if (tiers.isEmpty()) {
            return displayPrice;
        }
        for (PriceTierItem tier : tiers) {
            int min = tier.getMinQty() != null ? tier.getMinQty() : 0;
            Integer max = tier.getMaxQty();
            if (quantity >= min && (max == null || quantity <= max)) {
                BigDecimal discount = tier.getDiscountPercent() != null ? tier.getDiscountPercent() : BigDecimal.ZERO;
                return displayPrice.multiply(BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))).setScale(2, RoundingMode.HALF_UP);
            }
        }
        return displayPrice;
    }

    @Transactional(readOnly = true)
    public PriceTiersSettingsResponse getPriceTiers() {
        List<PriceTierItem> tiers = getPriceTiersList();
        return PriceTiersSettingsResponse.builder().tiers(tiers != null ? tiers : getDefaultPriceTiers()).build();
    }

    @Transactional
    public PriceTiersSettingsResponse updatePriceTiers(PriceTiersSettingsRequest request) {
        if (request == null || request.getTiers() == null || request.getTiers().isEmpty()) {
            throw new IllegalArgumentException("Список уровней цен не может быть пустым");
        }
        CompanySettings settings = companySettingsRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> companySettingsRepository.findFirstByOrderByIdAsc().orElse(null));
        if (settings == null) {
            settings = CompanySettings.builder()
                    .name("Мой магазин")
                    .country("Russia")
                    .isActive(true)
                    .build();
        }
        try {
            settings.setPriceTiersJson(objectMapper.writeValueAsString(request.getTiers()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректные данные уровней цен", e);
        }
        settings = companySettingsRepository.save(settings);
        return PriceTiersSettingsResponse.builder().tiers(request.getTiers()).build();
    }

    private List<PriceTierItem> getPriceTiersList() {
        CompanySettings settings = companySettingsRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> companySettingsRepository.findFirstByOrderByIdAsc().orElse(null));
        if (settings == null || settings.getPriceTiersJson() == null || settings.getPriceTiersJson().isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(settings.getPriceTiersJson(), PRICE_TIERS_TYPE);
        } catch (Exception e) {
            return null;
        }
    }

    private List<PriceTierItem> getDefaultPriceTiers() {
        List<PriceTierItem> list = new ArrayList<>();
        list.add(PriceTierItem.builder().minQty(1).maxQty(10).discountPercent(BigDecimal.ZERO).label("Розничная").build());
        list.add(PriceTierItem.builder().minQty(11).maxQty(100).discountPercent(BigDecimal.TEN).label("Мелкий опт").build());
        list.add(PriceTierItem.builder().minQty(101).maxQty(null).discountPercent(BigDecimal.valueOf(15)).label("Крупный опт").build());
        return list;
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

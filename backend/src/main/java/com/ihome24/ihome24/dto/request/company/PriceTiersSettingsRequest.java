package com.ihome24.ihome24.dto.request.company;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PriceTiersSettingsRequest {

    @NotNull(message = "Список уровней цен обязателен")
    private List<@Valid PriceTierItem> tiers;
}

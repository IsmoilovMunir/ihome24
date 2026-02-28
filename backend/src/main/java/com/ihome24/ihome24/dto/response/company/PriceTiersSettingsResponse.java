package com.ihome24.ihome24.dto.response.company;

import com.ihome24.ihome24.dto.request.company.PriceTierItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceTiersSettingsResponse {

    private List<PriceTierItem> tiers;
}

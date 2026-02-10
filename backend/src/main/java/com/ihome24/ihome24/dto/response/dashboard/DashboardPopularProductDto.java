package com.ihome24.ihome24.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardPopularProductDto {

    private Long productId;
    private String productName;
    private long totalQuantity;
    private String imageUrl;
}

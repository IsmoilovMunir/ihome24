package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

@Data
public class BarcodesRequest {
    private String skuBarcode;
    private String ean13;
}

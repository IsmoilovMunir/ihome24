package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeliveryRequest {
    private List<String> methods = new ArrayList<>();
    private String deliveryDays;
}

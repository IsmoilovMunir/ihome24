package com.ihome24.ihome24.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long order;
    private String customer;
    private String email;
    private String phone;
    private String address;
    private String avatar;
    private Integer payment;
    private String status;
    private BigDecimal spent;
    private String method;
    private String date;
    private String methodNumber;
    private List<OrderItemResponse> items;
}
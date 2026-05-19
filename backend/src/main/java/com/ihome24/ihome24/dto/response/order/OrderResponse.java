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
    /** ID пользователя в разделе «Клиенты» (роль users), если найден по email/телефону */
    private Long customerId;
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
    private String companyName;
    private String companyInn;
    private String companyKpp;
    private String companyAddress;
    private String companyOgrn;
    private String companyOkpo;
    private String companyCorrAccount;
    private String companyBik;
    private String companySettlementAccount;
    private List<OrderItemResponse> items;
}
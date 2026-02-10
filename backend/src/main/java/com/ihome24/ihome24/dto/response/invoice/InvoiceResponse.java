package com.ihome24.ihome24.dto.response.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private Long id;
    private ClientInfo client;
    private BigDecimal total;
    private String issuedDate;
    private BigDecimal balance;
    private String invoiceStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private String name;
        private String companyEmail;
    }
}

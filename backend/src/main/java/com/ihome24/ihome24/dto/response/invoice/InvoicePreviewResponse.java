package com.ihome24.ihome24.dto.response.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePreviewResponse {

    private InvoicePreview invoice;
    private PaymentDetails paymentDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoicePreview {
        private Long id;
        private LocalDateTime issuedDate;
        private LocalDateTime dueDate;
        private ClientInfo client;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientInfo {
        private String name;
        private String company;
        private String address;
        private String country;
        private String contact;
        private String companyEmail;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentDetails {
        private String totalDue;
        private String bankName;
        private String country;
        private String iban;
        private String swiftCode;
    }
}

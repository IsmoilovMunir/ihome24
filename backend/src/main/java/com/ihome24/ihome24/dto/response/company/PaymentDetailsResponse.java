package com.ihome24.ihome24.dto.response.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetailsResponse {
    private String name;
    private String inn;
    private String kpp;
    private String bankName;
    private String bankAccount;
    private String correspondentAccount;
    private String bik;
}

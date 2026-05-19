package com.ihome24.ihome24.dto.response.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyPartySuggestion {
    private String name;
    private String inn;
    private String kpp;
    private String ogrn;
    private String okpo;
    private String address;
    private String corrAccount;
    private String bik;
    private String settlementAccount;
    /** LEGAL — юрлицо, INDIVIDUAL — ИП */
    private String type;
}

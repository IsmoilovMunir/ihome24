package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

@Data
public class CompanyRequest {
    private String name;
    private String inn;
    private String country = "Russia";
}

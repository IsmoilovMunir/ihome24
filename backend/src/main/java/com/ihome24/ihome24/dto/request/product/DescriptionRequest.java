package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DescriptionRequest {
    private String shortDescription;
    private String full;
    private List<String> benefits = new ArrayList<>();
}

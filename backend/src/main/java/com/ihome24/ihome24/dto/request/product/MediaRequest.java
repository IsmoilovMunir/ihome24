package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MediaRequest {
    private String mainImage;
    private List<String> gallery = new ArrayList<>();
    private String video;
}

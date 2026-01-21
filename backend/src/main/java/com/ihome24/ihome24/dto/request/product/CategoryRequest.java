package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

@Data
public class CategoryRequest {
    private Long id;
    private Long parentId;
}

package com.ihome24.ihome24.controller.publicapi.category;

import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryRestController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        // Фильтруем только активные категории
        List<CategoryResponse> activeCategories = categories.stream()
                .filter(cat -> cat.getIsActive() != null && cat.getIsActive())
                .toList();
        return ResponseEntity.ok(activeCategories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        // Проверяем, что категория активна
        if (category.getIsActive() == null || !category.getIsActive()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }
}

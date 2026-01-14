package com.ihome24.ihome24.controller.admin.category;

import com.ihome24.ihome24.dto.request.category.CategoryRequest;
import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping(produces = "text/html")
    public String showAddCategoryForm(Model model,
                                      @RequestParam(required = false) Boolean success,
                                      CsrfToken csrfToken) {
        if (success != null && success) {
            model.addAttribute("success", true);
        }
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        // Загружаем все категории для выбора родительской
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "add-category";
    }

    @GetMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping(consumes = "application/json")
    @ResponseBody
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public String createCategoryFromForm(@Valid @ModelAttribute CategoryRequest request,
                                         RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(request);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/api/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/api/admin/categories";
        }
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                          @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

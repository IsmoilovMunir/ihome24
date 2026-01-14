package com.ihome24.ihome24.controller.admin.category;

import com.ihome24.ihome24.dto.request.category.CategoryRequest;
import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}

package com.ihome24.ihome24.controller.admin.product;

import com.ihome24.ihome24.dto.request.product.ProductRequest;
import com.ihome24.ihome24.service.category.CategoryService;
import com.ihome24.ihome24.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping(produces = "text/html")
    public String showAddProductForm(Model model, 
                                     @RequestParam(required = false) Boolean success,
                                     CsrfToken csrfToken) {
        if (success != null && success) {
            model.addAttribute("success", true);
        }
        // Добавляем CSRF токен в модель для использования в шаблоне
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        // Загружаем все категории для выбора
        model.addAttribute("categories", categoryService.getAllCategories());
        return "add-product";
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public String createProductFromForm(@Valid @ModelAttribute ProductRequest request, 
                                       RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(request);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/api/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/api/admin/products";
        }
    }
}

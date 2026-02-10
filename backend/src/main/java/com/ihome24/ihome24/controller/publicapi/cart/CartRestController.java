package com.ihome24.ihome24.controller.publicapi.cart;

import com.ihome24.ihome24.dto.request.cart.CartValidateRequest;
import com.ihome24.ihome24.dto.response.cart.CartValidateResponse;
import com.ihome24.ihome24.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartRestController {

    private final CartService cartService;

    /**
     * Валидирует корзину: проверяет существование и активность товаров.
     * Возвращает актуальные данные по валидным товарам и список ID удалённых/неактивных.
     */
    @PostMapping("/validate")
    public ResponseEntity<CartValidateResponse> validateCart(@Valid @RequestBody CartValidateRequest request) {
        CartValidateResponse response = cartService.validateCart(request);
        return ResponseEntity.ok(response);
    }
}

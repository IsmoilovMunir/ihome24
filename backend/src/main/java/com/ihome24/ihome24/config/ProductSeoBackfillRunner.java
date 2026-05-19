package com.ihome24.ihome24.config;

import com.ihome24.ihome24.service.product.ProductSeoBackfillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * B-1: при старте приложения заполняет slug/meta/og_image для товаров без SEO-данных.
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class ProductSeoBackfillRunner implements ApplicationRunner {

    private final ProductSeoBackfillService productSeoBackfillService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            productSeoBackfillService.backfillIfNeeded();
        } catch (Exception e) {
            // Не роняем prod, если миграция slug ещё не применена (changelog-sync без SQL)
            log.error("SEO backfill пропущен: {}. Добавьте products.slug (Liquibase V011 или SQL) и перезапустите.",
                    e.getMessage());
        }
    }
}

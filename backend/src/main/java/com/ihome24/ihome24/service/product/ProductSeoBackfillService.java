package com.ihome24.ihome24.service.product;

import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * B-1: автозаполнение slug и og_image для существующих товаров (идемпотентно при старте).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSeoBackfillService {

    private final ProductRepository productRepository;
    private final ProductSeoService productSeoService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void backfillIfNeeded() {
        List<Product> products = productRepository.findAll();
        int slugUpdated = 0;
        int ogUpdated = 0;

        for (Product product : products) {
            boolean changed = false;
            String slug = product.getSlug();
            if (slug == null || slug.isBlank() || slug.startsWith("product-")) {
                String generated = productSeoService.generateSlugForProduct(product);
                if (generated != null && !generated.equals(slug)) {
                    product.setSlug(generated);
                    changed = true;
                    slugUpdated++;
                }
            }
            if (product.getOgImage() == null || product.getOgImage().isBlank()) {
                String og = productSeoService.resolveOgImage(product);
                if (og != null) {
                    product.setOgImage(og);
                    changed = true;
                    ogUpdated++;
                }
            }
            if (product.getMetaTitle() == null || product.getMetaTitle().isBlank()) {
                product.setMetaTitle(productSeoService.generateMetaTitle(product.getName()));
                changed = true;
            }
            if (product.getMetaDescription() == null || product.getMetaDescription().isBlank()) {
                product.setMetaDescription(
                        productSeoService.generateMetaDescription(product.getName(), product.getPrice()));
                changed = true;
            }
            if (changed) {
                productRepository.save(product);
            }
        }

        if (slugUpdated > 0 || ogUpdated > 0) {
            log.info("SEO backfill: обновлено slug={}, og_image={}", slugUpdated, ogUpdated);
        }

        ensureSlugNotNullConstraint();
    }

    private void ensureSlugNotNullConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE products ALTER COLUMN slug SET NOT NULL");
            log.info("SEO backfill: ограничение NOT NULL на products.slug применено");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("already") || msg.contains("уже")) {
                return;
            }
            log.debug("NOT NULL на slug: {}", msg);
        }
    }
}

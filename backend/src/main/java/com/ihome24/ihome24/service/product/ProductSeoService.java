package com.ihome24.ihome24.service.product;

import com.ihome24.ihome24.dto.request.product.ProductSeoPatchRequest;
import com.ihome24.ihome24.exception.ProductSlugInvalidException;
import com.ihome24.ihome24.dto.request.product.ReturnsRequest;
import com.ihome24.ihome24.dto.request.product.SeoRequest;
import com.ihome24.ihome24.dto.response.product.ReturnsResponse;
import com.ihome24.ihome24.dto.response.product.SeoResponse;
import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.entity.product.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductSeoService {

    public static final int META_TITLE_MAX_LENGTH = 120;
    public static final int META_TITLE_RECOMMENDED_MAX = 65;
    public static final int META_DESCRIPTION_MAX_LENGTH = 300;

    private final SlugService slugService;
    private final ProductSlugRedirectService slugRedirectService;

    @Transactional
    public void applySeoAndReturns(Product product, SeoRequest seo, ReturnsRequest returns,
                                   String productTitle, String description, BigDecimal price,
                                   String mainImageUrl) {
        applyReturns(product, returns);
        applySeo(product, seo, productTitle, description, price, mainImageUrl);
    }

    public void applySeoAndReturns(Product product, SeoRequest seo, ReturnsRequest returns,
                                   String productTitle, String description, BigDecimal price) {
        applySeoAndReturns(product, seo, returns, productTitle, description, price, product.getImageUrl());
    }

    public void applyReturns(Product product, ReturnsRequest returns) {
        if (returns == null) {
            return;
        }
        if (returns.getAllowed() != null) {
            product.setReturnsAllowed(returns.getAllowed());
        }
        if (returns.getDays() != null) {
            product.setReturnsDays(returns.getDays());
        }
        if (returns.getConditions() != null) {
            product.setReturnsConditions(returns.getConditions().isBlank() ? null : returns.getConditions().trim());
        }
    }

    @Transactional
    public void applySeo(Product product, SeoRequest seo, String productTitle, String description,
                         BigDecimal price, String mainImageUrl) {
        String title = productTitle != null ? productTitle.trim() : product.getName();
        BigDecimal displayPrice = price != null ? price : product.getPrice();

        String oldSlug = product.getSlug();
        String newSlug = resolveNewSlug(product, seo, title);
        if (newSlug != null && !newSlug.equals(oldSlug)) {
            applySlugChange(product, newSlug);
        }

        if (seo != null) {
            if (seo.getMetaTitle() != null && !seo.getMetaTitle().isBlank()) {
                product.setMetaTitle(trimMetaTitle(seo.getMetaTitle()));
            } else if (seo.getMetaTitle() != null) {
                product.setMetaTitle(null);
            }
            if (seo.getMetaDescription() != null && !seo.getMetaDescription().isBlank()) {
                product.setMetaDescription(trimMetaDescription(seo.getMetaDescription(), false));
            } else if (seo.getMetaDescription() != null) {
                product.setMetaDescription(null);
            }
        }

        if (product.getSlug() == null || product.getSlug().isBlank()) {
            product.setSlug(slugService.generateSlug(title, product.getId()));
        }
        if (product.getMetaTitle() == null || product.getMetaTitle().isBlank()) {
            product.setMetaTitle(generateMetaTitle(title));
        }
        if (product.getMetaDescription() == null || product.getMetaDescription().isBlank()) {
            product.setMetaDescription(generateMetaDescription(title, displayPrice));
        }
        syncOgImage(product, mainImageUrl);
    }

    public String generateSlugForProduct(Product product) {
        return slugService.generateSlug(product.getName(), product.getId());
    }

    /**
     * B-3: обновление только SEO-полей (slug, metaTitle, metaDescription).
     *
     * @return предупреждения (не блокируют сохранение)
     */
    @Transactional
    public SeoPatchOutcome patchSeoFields(Product product, ProductSeoPatchRequest request) {
        List<String> warnings = new ArrayList<>();
        boolean redirectCreated = false;

        if (request.getSlug() != null) {
            String oldSlug = product.getSlug();
            String newSlug;
            if (request.getSlug().isBlank()) {
                newSlug = slugService.generateSlug(product.getName(), product.getId());
            } else {
                newSlug = slugService.normalizeWithoutUniqueness(request.getSlug());
                if (oldSlug == null || !oldSlug.equals(newSlug)) {
                    slugService.assertSlugAvailable(newSlug, product.getId());
                }
            }
            applySlugChange(product, newSlug);
            if (oldSlug != null && !oldSlug.isBlank() && newSlug != null && !oldSlug.equals(newSlug)) {
                redirectCreated = true;
            }
        }

        if (request.getMetaTitle() != null) {
            if (request.getMetaTitle().isBlank()) {
                product.setMetaTitle(null);
            } else {
                String title = trimMetaTitle(request.getMetaTitle());
                product.setMetaTitle(title);
                if (title != null && title.length() > META_TITLE_RECOMMENDED_MAX) {
                    warnings.add("Meta Title длиннее " + META_TITLE_RECOMMENDED_MAX
                            + " символов — в поисковой выдаче обычно видна только часть заголовка");
                }
            }
        }

        if (request.getMetaDescription() != null) {
            if (request.getMetaDescription().isBlank()) {
                product.setMetaDescription(null);
            } else {
                product.setMetaDescription(trimMetaDescription(request.getMetaDescription(), false));
            }
        }

        return new SeoPatchOutcome(warnings, redirectCreated);
    }

    private void applySlugChange(Product product, String newSlug) {
        String oldSlug = product.getSlug();
        if (newSlug != null && !newSlug.equals(oldSlug)) {
            if (oldSlug != null && !oldSlug.isBlank()) {
                slugRedirectService.registerSlugChange(oldSlug, newSlug);
            }
            product.setSlug(newSlug);
        }
    }

    public String resolveOgImage(Product product) {
        if (product.getOgImage() != null && !product.getOgImage().isBlank()) {
            return product.getOgImage();
        }
        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            return product.getImageUrl();
        }
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            return product.getImages().stream()
                    .filter(img -> img.getImageUrl() != null && !img.getImageUrl().isBlank())
                    .min(Comparator
                            .comparing((ProductImage img) -> Boolean.TRUE.equals(img.getIsPrimary()) ? 0 : 1)
                            .thenComparing(img -> img.getSortOrder() != null ? img.getSortOrder() : 0))
                    .map(ProductImage::getImageUrl)
                    .orElse(null);
        }
        return null;
    }

    private void syncOgImage(Product product, String mainImageUrl) {
        if (product.getOgImage() != null && !product.getOgImage().isBlank()) {
            return;
        }
        if (mainImageUrl != null && !mainImageUrl.isBlank()) {
            product.setOgImage(mainImageUrl);
            return;
        }
        String resolved = resolveOgImage(product);
        if (resolved != null) {
            product.setOgImage(resolved);
        }
    }

    private String resolveNewSlug(Product product, SeoRequest seo, String title) {
        if (seo != null && seo.getSlug() != null) {
            if (seo.getSlug().isBlank()) {
                return slugService.generateSlug(title, product.getId());
            }
            return slugService.normalizeAndValidate(seo.getSlug(), product.getId());
        }
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            return slugService.generateSlug(title, product.getId());
        }
        return null;
    }

    public SeoResponse toSeoResponse(Product product) {
        String slugPart = effectiveSlugPart(product);
        return SeoResponse.builder()
                .slug(slugPart)
                .pathSegment(slugPart)
                .metaTitle(effectiveMetaTitle(product))
                .metaDescription(effectiveMetaDescription(product))
                .ogImage(resolveOgImage(product))
                .storedSlug(blankToNull(product.getSlug()))
                .storedMetaTitle(blankToNull(product.getMetaTitle()))
                .storedMetaDescription(blankToNull(product.getMetaDescription()))
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public ReturnsResponse toReturnsResponse(Product product) {
        return ReturnsResponse.builder()
                .allowed(product.getReturnsAllowed() != null ? product.getReturnsAllowed() : true)
                .days(product.getReturnsDays() != null ? product.getReturnsDays() : 14)
                .conditions(product.getReturnsConditions())
                .build();
    }

    public String buildProductPath(Product product) {
        return "/product/" + effectiveSlugPart(product);
    }

    public String effectiveSlugPart(Product product) {
        if (product.getSlug() != null && !product.getSlug().isBlank()) {
            return product.getSlug();
        }
        if (product.getId() != null) {
            return slugService.generateSlug(product.getName(), product.getId());
        }
        return slugService.generateSlug(product.getName());
    }

    public String effectiveMetaTitle(Product product) {
        if (product.getMetaTitle() != null && !product.getMetaTitle().isBlank()) {
            return product.getMetaTitle();
        }
        return generateMetaTitle(product.getName());
    }

    public String effectiveMetaDescription(Product product) {
        if (product.getMetaDescription() != null && !product.getMetaDescription().isBlank()) {
            return product.getMetaDescription();
        }
        return generateMetaDescription(product.getName(), product.getPrice());
    }

    public String generateMetaTitle(String name) {
        String n = name != null ? name.trim() : "";
        String title = n.isEmpty() ? "Купить в ihome24.ru" : n + " — купить в ihome24.ru";
        if (title.length() > META_TITLE_MAX_LENGTH) {
            return title.substring(0, META_TITLE_MAX_LENGTH);
        }
        return title;
    }

    public String generateMetaDescription(String name, BigDecimal price) {
        String n = name != null ? name.trim() : "";
        if (n.isEmpty()) {
            return trimMetaDescription("Доставка. Гарантия. Заказать на ihome24.ru.", true);
        }
        String priceStr = formatPriceRub(price);
        String generated = n + " по цене " + priceStr + " ₽. Доставка. Гарантия. Заказать на ihome24.ru.";
        return trimMetaDescription(generated, true);
    }

    private String formatPriceRub(BigDecimal price) {
        if (price == null) {
            return "0";
        }
        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("ru", "RU"));
        return nf.format(price.setScale(0, java.math.RoundingMode.HALF_UP));
    }

    private String trimMetaDescription(String value, boolean truncateOnly) {
        if (value == null) {
            return "";
        }
        String s = value.trim();
        if (s.isEmpty()) {
            return "";
        }
        if (s.length() > META_DESCRIPTION_MAX_LENGTH) {
            if (truncateOnly) {
                return s.substring(0, META_DESCRIPTION_MAX_LENGTH);
            }
            throw new IllegalArgumentException(
                    "Meta Description не должен превышать " + META_DESCRIPTION_MAX_LENGTH + " символов");
        }
        return s;
    }

    private String trimMetaTitle(String value) {
        if (value == null) {
            return null;
        }
        String s = value.trim();
        if (s.isEmpty()) {
            return null;
        }
        if (s.length() > META_TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Meta Title не должен превышать " + META_TITLE_MAX_LENGTH + " символов");
        }
        return s;
    }
}

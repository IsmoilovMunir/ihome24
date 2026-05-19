package com.ihome24.ihome24.service.product;

import com.ihome24.ihome24.entity.product.ProductSlugRedirect;
import com.ihome24.ihome24.repository.product.ProductSlugRedirectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * B-4: регистрация 301-редиректов при смене slug без цепочек.
 */
@Service
@RequiredArgsConstructor
public class ProductSlugRedirectService {

    private final ProductSlugRedirectRepository redirectRepository;

    /**
     * При смене slug товара: old→new и схлопление всех записей, указывавших на old как на промежуточный.
     */
    @Transactional
    public void registerSlugChange(String oldSlug, String newSlug) {
        String oldNorm = normalize(oldSlug);
        String newNorm = normalize(newSlug);
        if (oldNorm.isEmpty() || newNorm.isEmpty() || oldNorm.equals(newNorm)) {
            return;
        }

        // Новый slug — канонический; не должен быть ни old, ни target чужих редиректов
        redirectRepository.deleteByOldSlug(newNorm);

        // Схлопнуть цепочки: old1→old2 + смена old2→new ⇒ old1→new
        redirectRepository.updateNewSlugWhereNewSlugWas(oldNorm, newNorm);

        // Обновить или создать прямой редирект old→new
        redirectRepository.findByOldSlug(oldNorm).ifPresentOrElse(
                existing -> {
                    existing.setNewSlug(newNorm);
                    redirectRepository.save(existing);
                },
                () -> redirectRepository.save(ProductSlugRedirect.builder()
                        .oldSlug(oldNorm)
                        .newSlug(newNorm)
                        .build())
        );
    }

    /**
     * Разрешить старый slug в актуальный (один hop, без цепочек в БД).
     */
    @Transactional(readOnly = true)
    public Optional<String> resolveNewSlug(String requestedSlug) {
        String key = normalize(requestedSlug);
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return redirectRepository.findByOldSlug(key).map(ProductSlugRedirect::getNewSlug);
    }

    public String buildCanonicalPath(String slug) {
        return "/product/" + normalize(slug);
    }

    private String normalize(String slug) {
        if (slug == null) {
            return "";
        }
        return slug.trim().toLowerCase(Locale.ROOT);
    }
}

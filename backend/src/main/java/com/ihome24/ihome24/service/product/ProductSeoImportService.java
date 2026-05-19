package com.ihome24.ihome24.service.product;

import com.ihome24.ihome24.dto.response.product.ProductSeoImportResult;
import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.ihome24.ihome24.util.ProductSeoCsvUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductSeoImportService {

    private final ProductRepository productRepository;
    private final ProductSeoService productSeoService;
    private final SlugService slugService;

    @Transactional
    public ProductSeoImportResult importCsv(MultipartFile file) {
        ProductSeoImportResult.ProductSeoImportResultBuilder result = ProductSeoImportResult.builder()
                .updated(0)
                .skipped(0)
                .errors(new ArrayList<>());

        if (file == null || file.isEmpty()) {
            result.errors(List.of("Файл не выбран"));
            return result.build();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.errors(List.of("Пустой CSV"));
                return result.build();
            }

            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            List<String> headers = ProductSeoCsvUtils.parseLine(headerLine);
            int skuIdx = ProductSeoCsvUtils.indexOfHeader(headers, "sku");
            int idIdx = ProductSeoCsvUtils.indexOfHeader(headers, "id");
            int slugIdx = ProductSeoCsvUtils.indexOfHeader(headers, "slug", "url_slug", "seo_slug");
            int titleIdx = ProductSeoCsvUtils.indexOfHeader(headers, "meta_title", "metatitle", "title");
            int descIdx = ProductSeoCsvUtils.indexOfHeader(headers, "meta_description", "metadescription", "description");

            if (skuIdx < 0 && idIdx < 0) {
                result.errors(List.of("Нужна колонка sku или id"));
                return result.build();
            }

            String line;
            int row = 1;
            int updated = 0;
            int skipped = 0;
            List<String> errors = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) {
                    continue;
                }
                List<String> cols = ProductSeoCsvUtils.parseLine(line);
                try {
                    Optional<Product> productOpt = findProduct(cols, skuIdx, idIdx);
                    if (productOpt.isEmpty()) {
                        skipped++;
                        errors.add("Строка " + row + ": товар не найден");
                        continue;
                    }
                    Product product = productOpt.get();
                    boolean changed = false;

                    if (slugIdx >= 0 && slugIdx < cols.size()) {
                        String slug = cols.get(slugIdx);
                        if (slug != null && !slug.isBlank()) {
                            product.setSlug(slugService.normalizeAndValidate(slug, product.getId()));
                            changed = true;
                        }
                    }
                    if (titleIdx >= 0 && titleIdx < cols.size()) {
                        String title = cols.get(titleIdx);
                        if (title != null && !title.isBlank()) {
                            String trimmed = title.trim();
                            if (trimmed.length() > ProductSeoService.META_TITLE_MAX_LENGTH) {
                                trimmed = trimmed.substring(0, ProductSeoService.META_TITLE_MAX_LENGTH);
                            }
                            product.setMetaTitle(trimmed);
                            changed = true;
                        }
                    }
                    if (descIdx >= 0 && descIdx < cols.size()) {
                        String desc = cols.get(descIdx);
                        if (desc != null && !desc.isBlank()) {
                            String trimmedDesc = desc.trim();
                            if (trimmedDesc.length() > ProductSeoService.META_DESCRIPTION_MAX_LENGTH) {
                                trimmedDesc = trimmedDesc.substring(0, ProductSeoService.META_DESCRIPTION_MAX_LENGTH);
                            }
                            product.setMetaDescription(trimmedDesc);
                            changed = true;
                        }
                    }

                    if (changed) {
                        productSeoService.applySeo(product, null, product.getName(),
                                product.getDescription(), product.getPrice(), product.getImageUrl());
                        productRepository.save(product);
                        updated++;
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    skipped++;
                    errors.add("Строка " + row + ": " + e.getMessage());
                }
            }

            return result.updated(updated).skipped(skipped).errors(errors).build();
        } catch (Exception e) {
            return result.errors(List.of("Ошибка чтения CSV: " + e.getMessage())).build();
        }
    }

    private Optional<Product> findProduct(List<String> cols, int skuIdx, int idIdx) {
        if (skuIdx >= 0 && skuIdx < cols.size()) {
            String sku = cols.get(skuIdx);
            if (sku != null && !sku.isBlank()) {
                return productRepository.findBySku(sku.trim());
            }
        }
        if (idIdx >= 0 && idIdx < cols.size()) {
            String idStr = cols.get(idIdx);
            if (idStr != null && !idStr.isBlank()) {
                try {
                    long id = Long.parseLong(idStr.trim());
                    return productRepository.findById(id);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return Optional.empty();
    }

}

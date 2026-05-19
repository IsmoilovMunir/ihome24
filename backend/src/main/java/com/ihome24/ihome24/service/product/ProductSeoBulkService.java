package com.ihome24.ihome24.service.product;

import com.ihome24.ihome24.dto.request.product.ProductSeoPatchRequest;
import com.ihome24.ihome24.dto.response.product.ProductSeoGenerateAllResult;
import com.ihome24.ihome24.dto.response.product.ProductSeoImportResult;
import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.exception.ProductSlugConflictException;
import com.ihome24.ihome24.repository.product.ProductRepository;
import com.ihome24.ihome24.util.ProductSeoCsvUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * B-5: массовая автогенерация, экспорт и импорт SEO (CSV: id,slug,meta_title,meta_description).
 */
@Service
@RequiredArgsConstructor
public class ProductSeoBulkService {

    private final ProductRepository productRepository;
    private final ProductSeoService productSeoService;
    private final SlugService slugService;

    @Transactional
    public ProductSeoGenerateAllResult generateAll() {
        List<Product> products = productRepository.findAll();
        int productsUpdated = 0;
        int slugsGenerated = 0;
        int metaTitlesGenerated = 0;
        int metaDescriptionsGenerated = 0;

        for (Product product : products) {
            boolean changed = false;

            if (needsSlugGeneration(product)) {
                product.setSlug(productSeoService.generateSlugForProduct(product));
                slugsGenerated++;
                changed = true;
            }
            if (ProductSeoCsvUtils.isBlank(product.getMetaTitle())) {
                product.setMetaTitle(productSeoService.generateMetaTitle(product.getName()));
                metaTitlesGenerated++;
                changed = true;
            }
            if (ProductSeoCsvUtils.isBlank(product.getMetaDescription())) {
                product.setMetaDescription(
                        productSeoService.generateMetaDescription(product.getName(), product.getPrice()));
                metaDescriptionsGenerated++;
                changed = true;
            }

            if (changed) {
                productRepository.save(product);
                productsUpdated++;
            }
        }

        return ProductSeoGenerateAllResult.builder()
                .productsUpdated(productsUpdated)
                .slugsGenerated(slugsGenerated)
                .metaTitlesGenerated(metaTitlesGenerated)
                .metaDescriptionsGenerated(metaDescriptionsGenerated)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        List<Product> products = productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append(ProductSeoCsvUtils.EXPORT_HEADER).append('\n');

        for (Product product : products) {
            sb.append(product.getId()).append(',');
            sb.append(ProductSeoCsvUtils.escapeField(nullToEmpty(product.getSlug()))).append(',');
            sb.append(ProductSeoCsvUtils.escapeField(nullToEmpty(product.getMetaTitle()))).append(',');
            sb.append(ProductSeoCsvUtils.escapeField(nullToEmpty(product.getMetaDescription()))).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public ProductSeoImportResult importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ProductSeoImportResult.builder()
                    .errors(List.of("Файл не выбран"))
                    .build();
        }

        int updated = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return ProductSeoImportResult.builder().errors(List.of("Пустой CSV")).build();
            }
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            List<String> headers = ProductSeoCsvUtils.parseLine(headerLine);
            int idIdx = ProductSeoCsvUtils.indexOfHeader(headers, "id");
            int slugIdx = ProductSeoCsvUtils.indexOfHeader(headers, "slug");
            int titleIdx = ProductSeoCsvUtils.indexOfHeader(headers, "meta_title");
            int descIdx = ProductSeoCsvUtils.indexOfHeader(headers, "meta_description");

            if (idIdx < 0) {
                return ProductSeoImportResult.builder()
                        .errors(List.of("Обязательная колонка: id"))
                        .build();
            }
            if (slugIdx < 0 || titleIdx < 0 || descIdx < 0) {
                return ProductSeoImportResult.builder()
                        .errors(List.of("Ожидаются колонки: id, slug, meta_title, meta_description"))
                        .build();
            }

            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) {
                    continue;
                }
                List<String> cols = ProductSeoCsvUtils.parseLine(line);
                try {
                    ImportRowResult rowResult = processImportRow(cols, idIdx, slugIdx, titleIdx, descIdx, row);
                    if (rowResult.updated()) {
                        updated++;
                    } else {
                        skipped++;
                    }
                    errors.addAll(rowResult.errors());
                } catch (Exception e) {
                    skipped++;
                    errors.add("Строка " + row + ": " + e.getMessage());
                }
            }

            return ProductSeoImportResult.builder()
                    .updated(updated)
                    .skipped(skipped)
                    .errors(errors)
                    .build();
        } catch (Exception e) {
            return ProductSeoImportResult.builder()
                    .errors(List.of("Ошибка чтения CSV: " + e.getMessage()))
                    .build();
        }
    }

    private ImportRowResult processImportRow(List<String> cols, int idIdx, int slugIdx, int titleIdx, int descIdx,
                                             int row) {
        List<String> rowErrors = new ArrayList<>();
        String idStr = ProductSeoCsvUtils.cell(cols, idIdx);
        if (ProductSeoCsvUtils.isBlank(idStr)) {
            rowErrors.add("Строка " + row + ": id обязателен");
            return new ImportRowResult(false, rowErrors);
        }

        long productId;
        try {
            productId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            rowErrors.add("Строка " + row + ": id должен быть числом");
            return new ImportRowResult(false, rowErrors);
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            rowErrors.add("Строка " + row + ": товар с id=" + productId + " не найден");
            return new ImportRowResult(false, rowErrors);
        }

        String slugRaw = ProductSeoCsvUtils.cell(cols, slugIdx);
        String titleRaw = ProductSeoCsvUtils.cell(cols, titleIdx);
        String descRaw = ProductSeoCsvUtils.cell(cols, descIdx);

        boolean hasSlug = !ProductSeoCsvUtils.isBlank(slugRaw);
        boolean hasTitle = !ProductSeoCsvUtils.isBlank(titleRaw);
        boolean hasDesc = !ProductSeoCsvUtils.isBlank(descRaw);

        if (!hasSlug && !hasTitle && !hasDesc) {
            rowErrors.add("Строка " + row + ": нет данных для обновления (slug, meta_title, meta_description пусты)");
            return new ImportRowResult(false, rowErrors);
        }

        Product product = productOpt.get();
        ProductSeoPatchRequest patch = new ProductSeoPatchRequest();

        if (hasSlug) {
            String normalized = slugService.normalizeWithoutUniqueness(slugRaw);
            if (!slugService.isValidSlugFormat(normalized)) {
                rowErrors.add("Строка " + row + ": неверный формат slug");
                return new ImportRowResult(false, rowErrors);
            }
            if (normalized.length() > SlugService.MAX_SLUG_LENGTH) {
                rowErrors.add("Строка " + row + ": slug длиннее " + SlugService.MAX_SLUG_LENGTH + " символов");
                return new ImportRowResult(false, rowErrors);
            }
            String currentSlug = product.getSlug();
            if (currentSlug == null || !currentSlug.equals(normalized)) {
                try {
                    slugService.assertSlugAvailable(normalized, product.getId());
                } catch (ProductSlugConflictException e) {
                    rowErrors.add("Строка " + row + ": slug «" + normalized + "» уже занят");
                    return new ImportRowResult(false, rowErrors);
                }
            }
            patch.setSlug(normalized);
        }

        if (hasTitle) {
            if (titleRaw.length() > ProductSeoService.META_TITLE_MAX_LENGTH) {
                rowErrors.add("Строка " + row + ": meta_title длиннее "
                        + ProductSeoService.META_TITLE_MAX_LENGTH + " символов");
                return new ImportRowResult(false, rowErrors);
            }
            patch.setMetaTitle(titleRaw);
        }

        if (hasDesc) {
            if (descRaw.length() > ProductSeoService.META_DESCRIPTION_MAX_LENGTH) {
                rowErrors.add("Строка " + row + ": meta_description длиннее "
                        + ProductSeoService.META_DESCRIPTION_MAX_LENGTH + " символов");
                return new ImportRowResult(false, rowErrors);
            }
            patch.setMetaDescription(descRaw);
        }

        if (!rowErrors.isEmpty()) {
            return new ImportRowResult(false, rowErrors);
        }

        productSeoService.patchSeoFields(product, patch);
        productRepository.save(product);
        return new ImportRowResult(true, rowErrors);
    }

    private boolean needsSlugGeneration(Product product) {
        String slug = product.getSlug();
        return ProductSeoCsvUtils.isBlank(slug) || slug.startsWith("product-");
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private record ImportRowResult(boolean updated, List<String> errors) {
    }
}

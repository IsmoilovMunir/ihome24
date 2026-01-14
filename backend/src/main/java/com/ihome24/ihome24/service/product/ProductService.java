package com.ihome24.ihome24.service.product;

import com.ihome24.ihome24.dto.request.product.*;
import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.entity.category.Category;
import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.entity.product.ProductVariant;
import com.ihome24.ihome24.entity.product.ProductImage;
import com.ihome24.ihome24.repository.category.CategoryRepository;
import com.ihome24.ihome24.repository.product.ProductRepository;
import com.ihome24.ihome24.service.company.CompanySettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CompanySettingsService companySettingsService;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        ProductInfoRequest productInfo = request.getProduct();
        
        // Проверка на уникальность SKU товара, если указан
        if (productInfo.getSku() != null && !productInfo.getSku().isEmpty()) {
            if (productRepository.existsBySku(productInfo.getSku())) {
                throw new IllegalArgumentException("Товар с таким SKU уже существует: " + productInfo.getSku());
            }
        }

        // Проверка на уникальность SKU вариантов
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (VariantRequest variant : request.getVariants()) {
                if (variant.getSku() != null && !variant.getSku().isEmpty()) {
                    if (productRepository.existsBySku(variant.getSku())) {
                        throw new IllegalArgumentException("Вариант с таким SKU уже существует: " + variant.getSku());
                    }
                }
            }
        }

        Product.ProductBuilder productBuilder = Product.builder()
                .name(productInfo.getTitle())
                .sku(productInfo.getSku())
                .status(request.getStatus() != null ? request.getStatus() : "draft");

        // Установка описания
        if (productInfo.getDescription() != null) {
            DescriptionRequest desc = productInfo.getDescription();
            String fullDescription = desc.getFull();
            if (fullDescription == null && desc.getShortDescription() != null) {
                fullDescription = desc.getShortDescription();
            }
            productBuilder.description(fullDescription);
        }

        // Установка базовой цены из первого варианта
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            VariantRequest firstVariant = request.getVariants().get(0);
            if (firstVariant.getPrice() != null && firstVariant.getPrice().getBase() != null) {
                productBuilder.price(firstVariant.getPrice().getBase());
                if (firstVariant.getPrice().getSale() != null) {
                    productBuilder.discountedPrice(firstVariant.getPrice().getSale());
                }
            }
            
            // Установка количества на складе из первого варианта
            if (firstVariant.getStock() != null && firstVariant.getStock().getQuantity() != null) {
                productBuilder.stockQuantity(firstVariant.getStock().getQuantity());
            }
        }

        // Установка категории
        if (productInfo.getCategory() != null && productInfo.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productInfo.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + productInfo.getCategory().getId()));
            productBuilder.category(category);
        }

        // Установка основного изображения
        if (productInfo.getMedia() != null && productInfo.getMedia().getMainImage() != null) {
            productBuilder.imageUrl(productInfo.getMedia().getMainImage());
        }

        // Информация о компании автоматически берется из настроек компании
        // (раздел "Информация о компании" в дашборде настроек)
        // Все товары автоматически привязываются к настройкам компании аккаунта

        Product product = productBuilder.build();

        // Добавление вариантов
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ProductVariant> variants = new ArrayList<>();
            for (VariantRequest variantRequest : request.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .optionName(variantRequest.getAttributes() != null ? 
                            variantRequest.getAttributes().getOrDefault("color", "") + " " + 
                            variantRequest.getAttributes().getOrDefault("size", "") : "")
                        .optionValue(variantRequest.getSku() != null ? variantRequest.getSku() : "")
                        .build();
                
                // Сохранение данных варианта (можно расширить ProductVariant entity для хранения полных данных)
                // Пока сохраняем только основные поля
                
                variants.add(variant);
            }
            product.setVariants(variants);
        }

        // Добавление изображений из галереи
        if (productInfo.getMedia() != null && productInfo.getMedia().getGallery() != null) {
            List<ProductImage> images = new ArrayList<>();
            int sortOrder = 0;
            for (String imageUrl : productInfo.getMedia().getGallery()) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    ProductImage image = ProductImage.builder()
                            .product(product)
                            .imageUrl(imageUrl)
                            .sortOrder(sortOrder++)
                            .isPrimary(sortOrder == 1)
                            .build();
                    images.add(image);
                }
            }
            product.setImages(images);
        }

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct, request);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllWithCategory();
        return products.stream()
                .map(p -> mapToResponse(p, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + id));
        return mapToResponse(product, null);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + id));

        ProductInfoRequest productInfo = request.getProduct();

        // Обновление основных полей
        if (productInfo.getTitle() != null) {
            product.setName(productInfo.getTitle());
        }
        if (productInfo.getSku() != null) {
            // Проверка на уникальность SKU, если он изменился
            if (!productInfo.getSku().equals(product.getSku()) && 
                productRepository.existsBySku(productInfo.getSku())) {
                throw new IllegalArgumentException("Товар с таким SKU уже существует: " + productInfo.getSku());
            }
            product.setSku(productInfo.getSku());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        // Обновление описания
        if (productInfo.getDescription() != null) {
            DescriptionRequest desc = productInfo.getDescription();
            String fullDescription = desc.getFull();
            if (fullDescription == null && desc.getShortDescription() != null) {
                fullDescription = desc.getShortDescription();
            }
            product.setDescription(fullDescription);
        }

        // Обновление базовой цены из первого варианта
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            VariantRequest firstVariant = request.getVariants().get(0);
            if (firstVariant.getPrice() != null && firstVariant.getPrice().getBase() != null) {
                product.setPrice(firstVariant.getPrice().getBase());
                if (firstVariant.getPrice().getSale() != null) {
                    product.setDiscountedPrice(firstVariant.getPrice().getSale());
                }
            }
            
            // Обновление количества на складе из первого варианта
            if (firstVariant.getStock() != null && firstVariant.getStock().getQuantity() != null) {
                product.setStockQuantity(firstVariant.getStock().getQuantity());
            }
        }

        // Обновление категории
        if (productInfo.getCategory() != null && productInfo.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productInfo.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + productInfo.getCategory().getId()));
            product.setCategory(category);
        }

        // Обновление основного изображения
        if (productInfo.getMedia() != null && productInfo.getMedia().getMainImage() != null) {
            product.setImageUrl(productInfo.getMedia().getMainImage());
        }

        // Обновление вариантов
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            // Удаляем старые варианты
            product.getVariants().clear();
            
            // Добавляем новые варианты
            List<ProductVariant> variants = new ArrayList<>();
            for (VariantRequest variantRequest : request.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .optionName(variantRequest.getAttributes() != null ? 
                            variantRequest.getAttributes().getOrDefault("color", "") + " " + 
                            variantRequest.getAttributes().getOrDefault("size", "") : "")
                        .optionValue(variantRequest.getSku() != null ? variantRequest.getSku() : "")
                        .build();
                variants.add(variant);
            }
            product.setVariants(variants);
        }

        // Обновление изображений из галереи
        if (productInfo.getMedia() != null && productInfo.getMedia().getGallery() != null) {
            // Удаляем старые изображения
            product.getImages().clear();
            
            // Добавляем новые изображения
            List<ProductImage> images = new ArrayList<>();
            int sortOrder = 0;
            for (String imageUrl : productInfo.getMedia().getGallery()) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    ProductImage image = ProductImage.builder()
                            .product(product)
                            .imageUrl(imageUrl)
                            .sortOrder(sortOrder++)
                            .isPrimary(sortOrder == 1)
                            .build();
                    images.add(image);
                }
            }
            product.setImages(images);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct, request);
    }

    private ProductResponse mapToResponse(Product product, ProductRequest originalRequest) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .discountedPrice(product.getDiscountedPrice())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .stockQuantity(product.getStockQuantity())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .taxEnabled(product.getTaxEnabled())
                .status(product.getStatus())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        // Добавляем информацию о категории
        if (product.getCategory() != null) {
            CategoryResponse categoryResponse = CategoryResponse.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .slug(product.getCategory().getSlug())
                    .description(product.getCategory().getDescription())
                    .imageUrl(product.getCategory().getImageUrl())
                    .isActive(product.getCategory().getIsActive())
                    .sortOrder(product.getCategory().getSortOrder())
                    .createdAt(product.getCategory().getCreatedAt())
                    .updatedAt(product.getCategory().getUpdatedAt())
                    .build();
            builder.category(categoryResponse);
        }

        // Заполняем расширенные поля из originalRequest, если он есть
        if (originalRequest != null && originalRequest.getProduct() != null) {
            ProductInfoRequest productInfo = originalRequest.getProduct();
            
            // Brand
            builder.brand(productInfo.getBrand());
            
            // Description
            if (productInfo.getDescription() != null) {
                builder.descriptionObj(ProductResponse.DescriptionResponse.builder()
                        .shortDescription(productInfo.getDescription().getShortDescription())
                        .full(productInfo.getDescription().getFull())
                        .benefits(productInfo.getDescription().getBenefits())
                        .build());
            }
            
            // Characteristics
            if (productInfo.getCharacteristics() != null && !productInfo.getCharacteristics().isEmpty()) {
                builder.characteristics(productInfo.getCharacteristics().stream()
                        .map(c -> ProductResponse.CharacteristicResponse.builder()
                                .key(c.getKey())
                                .name(c.getName())
                                .value(c.getValue())
                                .filterable(c.getFilterable())
                                .build())
                        .collect(Collectors.toList()));
            }
            
            // Media
            if (productInfo.getMedia() != null) {
                builder.media(ProductResponse.MediaResponse.builder()
                        .mainImage(productInfo.getMedia().getMainImage())
                        .gallery(productInfo.getMedia().getGallery())
                        .video(productInfo.getMedia().getVideo())
                        .build());
            }
            
            // Variants detailed
            if (originalRequest.getVariants() != null && !originalRequest.getVariants().isEmpty()) {
                builder.variantsDetailed(originalRequest.getVariants().stream()
                        .map(v -> {
                            ProductResponse.DetailedVariantResponse.DetailedVariantResponseBuilder variantBuilder = 
                                    ProductResponse.DetailedVariantResponse.builder()
                                    .variantId(v.getVariantId())
                                    .sku(v.getSku())
                                    .attributes(v.getAttributes());
                            
                            if (v.getPrice() != null) {
                                variantBuilder.price(ProductResponse.PriceResponse.builder()
                                        .base(v.getPrice().getBase())
                                        .sale(v.getPrice().getSale())
                                        .currency(v.getPrice().getCurrency())
                                        .vat(v.getPrice().getVat())
                                        .build());
                            }
                            
                            if (v.getStock() != null) {
                                variantBuilder.stock(ProductResponse.StockResponse.builder()
                                        .quantity(v.getStock().getQuantity())
                                        .build());
                            }
                            
                            if (v.getBarcodes() != null) {
                                variantBuilder.barcodes(ProductResponse.BarcodesResponse.builder()
                                        .skuBarcode(v.getBarcodes().getSkuBarcode())
                                        .ean13(v.getBarcodes().getEan13())
                                        .build());
                            }
                            
                            if (v.getLogistics() != null) {
                                ProductResponse.LogisticsResponse.LogisticsResponseBuilder logisticsBuilder = 
                                        ProductResponse.LogisticsResponse.builder()
                                        .weightKg(v.getLogistics().getWeightKg() != null ? 
                                                v.getLogistics().getWeightKg().toString() : null);
                                
                                if (v.getLogistics().getDimensionsCm() != null) {
                                    logisticsBuilder.dimensionsCm(ProductResponse.DimensionsResponse.builder()
                                            .length(v.getLogistics().getDimensionsCm().getLength() != null ? 
                                                    v.getLogistics().getDimensionsCm().getLength().toString() : null)
                                            .width(v.getLogistics().getDimensionsCm().getWidth() != null ? 
                                                    v.getLogistics().getDimensionsCm().getWidth().toString() : null)
                                            .height(v.getLogistics().getDimensionsCm().getHeight() != null ? 
                                                    v.getLogistics().getDimensionsCm().getHeight().toString() : null)
                                            .build());
                                }
                                
                                if (v.getLogistics().getDelivery() != null) {
                                    logisticsBuilder.delivery(ProductResponse.DeliveryResponse.builder()
                                            .methods(v.getLogistics().getDelivery().getMethods())
                                            .deliveryDays(v.getLogistics().getDelivery().getDeliveryDays())
                                            .build());
                                }
                                
                                variantBuilder.logistics(logisticsBuilder.build());
                            }
                            
                            return variantBuilder.build();
                        })
                        .collect(Collectors.toList()));
            }
            
            // Returns
            if (originalRequest.getReturns() != null) {
                builder.returns(ProductResponse.ReturnsResponse.builder()
                        .allowed(originalRequest.getReturns().getAllowed())
                        .days(originalRequest.getReturns().getDays())
                        .conditions(originalRequest.getReturns().getConditions())
                        .build());
            }
            
            // SEO
            if (originalRequest.getSeo() != null) {
                builder.seo(ProductResponse.SeoResponse.builder()
                        .slug(originalRequest.getSeo().getSlug())
                        .metaTitle(originalRequest.getSeo().getMetaTitle())
                        .metaDescription(originalRequest.getSeo().getMetaDescription())
                        .build());
            }
        } else {
            // Если originalRequest нет, пытаемся восстановить структуру из базы данных
            // Media из images
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                List<String> gallery = product.getImages().stream()
                        .map(img -> img.getImageUrl())
                        .collect(Collectors.toList());
                builder.media(ProductResponse.MediaResponse.builder()
                        .mainImage(product.getImageUrl())
                        .gallery(gallery)
                        .build());
            } else if (product.getImageUrl() != null) {
                builder.media(ProductResponse.MediaResponse.builder()
                        .mainImage(product.getImageUrl())
                        .gallery(new ArrayList<>())
                        .build());
            }
        }

        // Добавляем варианты (для обратной совместимости)
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            builder.variants(product.getVariants().stream()
                    .map(v -> com.ihome24.ihome24.dto.response.product.VariantResponse.builder()
                            .id(v.getId())
                            .optionName(v.getOptionName())
                            .optionValue(v.getOptionValue())
                            .build())
                    .collect(Collectors.toList()));
        }

        // Добавляем изображения
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            builder.images(product.getImages().stream()
                    .map(img -> com.ihome24.ihome24.dto.response.product.ProductImageResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .sortOrder(img.getSortOrder())
                            .isPrimary(img.getIsPrimary())
                            .build())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}

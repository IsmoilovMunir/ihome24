package com.ihome24.ihome24.service.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome24.ihome24.dto.request.product.CharacteristicRequest;
import com.ihome24.ihome24.dto.request.product.DescriptionRequest;
import com.ihome24.ihome24.dto.request.product.ProductInfoRequest;
import com.ihome24.ihome24.dto.request.product.ProductRequest;
import com.ihome24.ihome24.dto.request.product.VariantRequest;
import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.dto.response.product.CharacteristicResponse;
import com.ihome24.ihome24.dto.response.product.ProductImageResponse;
import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.dto.response.product.VariantResponse;
import com.ihome24.ihome24.entity.category.Category;
import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.entity.product.ProductImage;
import com.ihome24.ihome24.repository.category.CategoryRepository;
import com.ihome24.ihome24.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        ProductInfoRequest productInfo = request.getProduct();
        if (productInfo == null) {
            throw new IllegalArgumentException("Информация о товаре обязательна");
        }

        // Проверка на уникальность SKU, если указан
        String sku = productInfo.getSku();
        if (sku != null && !sku.isEmpty()) {
            if (productRepository.existsBySku(sku)) {
                throw new IllegalArgumentException("Товар с таким SKU уже существует: " + sku);
            }
        }

        // Получаем данные из первого варианта, если он есть
        BigDecimal price;
        BigDecimal oldPrice = null;
        Integer stockQuantity = 0;
        
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Товар должен содержать хотя бы один вариант с ценой");
        }
        
        VariantRequest firstVariant = request.getVariants().get(0);
        if (firstVariant.getPrice() == null || firstVariant.getPrice().getBase() == null) {
            throw new IllegalArgumentException("Цена товара обязательна");
        }
        
        price = firstVariant.getPrice().getBase();
        oldPrice = firstVariant.getPrice().getSale();
        
        if (firstVariant.getStock() != null && firstVariant.getStock().getQuantity() != null) {
            stockQuantity = firstVariant.getStock().getQuantity();
        }

        // Получаем описание
        String description = null;
        if (productInfo.getDescription() != null) {
            DescriptionRequest desc = productInfo.getDescription();
            description = desc.getFull() != null ? desc.getFull() : desc.getShortDescription();
        }

        // Получаем изображения
        String imageUrl = null;
        List<String> galleryImages = new ArrayList<>();
        if (productInfo.getMedia() != null) {
            imageUrl = productInfo.getMedia().getMainImage();
            if (productInfo.getMedia().getGallery() != null) {
                galleryImages = productInfo.getMedia().getGallery();
            }
            // Если есть главное изображение, добавляем его первым в галерею
            if (imageUrl != null && !galleryImages.contains(imageUrl)) {
                galleryImages.add(0, imageUrl);
            }
        }

        // Сохраняем характеристики в JSON
        String characteristicsJson = null;
        if (productInfo.getCharacteristics() != null && !productInfo.getCharacteristics().isEmpty()) {
            try {
                characteristicsJson = objectMapper.writeValueAsString(productInfo.getCharacteristics());
            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка при сохранении характеристик: " + e.getMessage());
            }
        }

        // Сохраняем преимущества в JSON
        String benefitsJson = null;
        if (productInfo.getDescription() != null && productInfo.getDescription().getBenefits() != null 
                && !productInfo.getDescription().getBenefits().isEmpty()) {
            try {
                benefitsJson = objectMapper.writeValueAsString(productInfo.getDescription().getBenefits());
            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка при сохранении преимуществ: " + e.getMessage());
            }
        }

        // Сохраняем варианты в JSON
        String variantsJson = null;
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            try {
                variantsJson = objectMapper.writeValueAsString(request.getVariants());
            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка при сохранении вариантов: " + e.getMessage());
            }
        }

        // Определяем статус активности на основе статуса товара
        Boolean isActive = !"draft".equals(request.getStatus());
        Boolean isFeatured = false; // По умолчанию не избранный

        // Получаем категорию, если указана
        Category category = null;
        if (productInfo.getCategory() != null) {
            if (productInfo.getCategory().getId() != null) {
                // Если указан ID категории, используем её
                category = categoryRepository.findById(productInfo.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + productInfo.getCategory().getId() + " не найдена"));
            } else if (productInfo.getCategory().getParentId() != null) {
                // Если указан только parentId, используем родительскую категорию как категорию товара
                category = categoryRepository.findById(productInfo.getCategory().getParentId())
                        .orElseThrow(() -> new IllegalArgumentException("Родительская категория с ID " + productInfo.getCategory().getParentId() + " не найдена"));
            }
        }

        Product product = Product.builder()
                .name(productInfo.getTitle())
                .description(description)
                .price(price)
                .oldPrice(oldPrice)
                .sku(sku)
                .brand(productInfo.getBrand())
                .stockQuantity(stockQuantity)
                .isActive(isActive)
                .isFeatured(isFeatured)
                .imageUrl(imageUrl)
                .characteristicsJson(characteristicsJson)
                .benefitsJson(benefitsJson)
                .variantsJson(variantsJson)
                .status(request.getStatus())
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);

        // Сохраняем изображения галереи
        if (!galleryImages.isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            for (int i = 0; i < galleryImages.size(); i++) {
                String imgUrl = galleryImages.get(i);
                if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                    ProductImage productImage = ProductImage.builder()
                            .product(savedProduct)
                            .imageUrl(imgUrl)
                            .sortOrder(i)
                            .isPrimary(i == 0) // Первое изображение - главное
                            .build();
                    productImages.add(productImage);
                }
            }
            savedProduct.setImages(productImages);
            savedProduct = productRepository.save(savedProduct);
        }

        return mapToResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllWithCategory();
        return products.stream()
                .map(product -> {
                    // Инициализируем lazy коллекцию изображений
                    product.getImages().size();
                    return mapToResponse(product);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        List<Product> products = productRepository.findByIsActiveTrue();
        return products.stream()
                .map(product -> {
                    // Инициализируем lazy коллекцию изображений
                    product.getImages().size();
                    return mapToResponse(product);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар с ID " + id + " не найден"));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар с ID " + id + " не найден"));

        ProductInfoRequest productInfo = request.getProduct();
        if (productInfo == null) {
            throw new IllegalArgumentException("Информация о товаре обязательна");
        }

        // Проверка на уникальность SKU, если указан и изменился
        String sku = productInfo.getSku();
        if (sku != null && !sku.isEmpty() && !sku.equals(product.getSku())) {
            if (productRepository.existsBySku(sku)) {
                throw new IllegalArgumentException("Товар с таким SKU уже существует: " + sku);
            }
        }

        // Получаем данные из первого варианта, если он есть
        BigDecimal price;
        BigDecimal oldPrice = null;
        Integer stockQuantity = 0;
        
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Товар должен содержать хотя бы один вариант с ценой");
        }
        
        VariantRequest firstVariant = request.getVariants().get(0);
        if (firstVariant.getPrice() == null || firstVariant.getPrice().getBase() == null) {
            throw new IllegalArgumentException("Цена товара обязательна");
        }
        
        price = firstVariant.getPrice().getBase();
        oldPrice = firstVariant.getPrice().getSale();
        
        if (firstVariant.getStock() != null && firstVariant.getStock().getQuantity() != null) {
            stockQuantity = firstVariant.getStock().getQuantity();
        }

        // Получаем описание
        String description = null;
        if (productInfo.getDescription() != null) {
            DescriptionRequest desc = productInfo.getDescription();
            description = desc.getFull() != null ? desc.getFull() : desc.getShortDescription();
        }

        // Получаем изображения
        String imageUrl = null;
        List<String> galleryImages = new ArrayList<>();
        if (productInfo.getMedia() != null) {
            imageUrl = productInfo.getMedia().getMainImage();
            if (productInfo.getMedia().getGallery() != null) {
                galleryImages = productInfo.getMedia().getGallery();
            }
            // Если есть главное изображение, добавляем его первым в галерею
            if (imageUrl != null && !galleryImages.contains(imageUrl)) {
                galleryImages.add(0, imageUrl);
            }
        }

        // Сохраняем характеристики в JSON
        String characteristicsJson = null;
        if (productInfo.getCharacteristics() != null && !productInfo.getCharacteristics().isEmpty()) {
            try {
                characteristicsJson = objectMapper.writeValueAsString(productInfo.getCharacteristics());
            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка при сохранении характеристик: " + e.getMessage());
            }
        }

        // Сохраняем преимущества в JSON
        String benefitsJson = null;
        if (productInfo.getDescription() != null && productInfo.getDescription().getBenefits() != null 
                && !productInfo.getDescription().getBenefits().isEmpty()) {
            try {
                benefitsJson = objectMapper.writeValueAsString(productInfo.getDescription().getBenefits());
            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка при сохранении преимуществ: " + e.getMessage());
            }
        }

        // Сохраняем варианты в JSON
        String variantsJson = null;
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            try {
                variantsJson = objectMapper.writeValueAsString(request.getVariants());
            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка при сохранении вариантов: " + e.getMessage());
            }
        }

        // Определяем статус активности на основе статуса товара
        Boolean isActive = !"draft".equals(request.getStatus());

        // Получаем категорию, если указана
        Category category = null;
        if (productInfo.getCategory() != null) {
            if (productInfo.getCategory().getId() != null) {
                // Если указан ID категории, используем её
                category = categoryRepository.findById(productInfo.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + productInfo.getCategory().getId() + " не найдена"));
            } else if (productInfo.getCategory().getParentId() != null) {
                // Если указан только parentId, используем родительскую категорию как категорию товара
                category = categoryRepository.findById(productInfo.getCategory().getParentId())
                        .orElseThrow(() -> new IllegalArgumentException("Родительская категория с ID " + productInfo.getCategory().getParentId() + " не найдена"));
            }
        }

        // Обновляем поля товара
        product.setName(productInfo.getTitle());
        product.setDescription(description);
        product.setPrice(price);
        product.setOldPrice(oldPrice);
        product.setSku(sku);
        product.setBrand(productInfo.getBrand());
        product.setStockQuantity(stockQuantity);
        product.setIsActive(isActive);
        product.setImageUrl(imageUrl);
        product.setCharacteristicsJson(characteristicsJson);
        product.setBenefitsJson(benefitsJson);
        product.setVariantsJson(variantsJson);
        product.setStatus(request.getStatus());
        product.setCategory(category);

        // Обновляем изображения галереи - удаляем старые и создаем новые
        product.getImages().clear();
        if (!galleryImages.isEmpty()) {
            for (int i = 0; i < galleryImages.size(); i++) {
                String imgUrl = galleryImages.get(i);
                if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .imageUrl(imgUrl)
                            .sortOrder(i)
                            .isPrimary(i == 0) // Первое изображение - главное
                            .build();
                    product.getImages().add(productImage);
                }
            }
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Товар с ID " + id + " не найден");
        }
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .sku(product.getSku())
                .brand(product.getBrand())
                .stockQuantity(product.getStockQuantity())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        // Загружаем характеристики из JSON
        List<CharacteristicResponse> characteristics = new ArrayList<>();
        if (product.getCharacteristicsJson() != null && !product.getCharacteristicsJson().isEmpty()) {
            try {
                List<CharacteristicRequest> charRequests = objectMapper.readValue(
                        product.getCharacteristicsJson(),
                        new TypeReference<List<CharacteristicRequest>>() {}
                );
                characteristics = charRequests.stream()
                        .map(charReq -> CharacteristicResponse.builder()
                                .key(charReq.getKey())
                                .name(charReq.getName())
                                .value(charReq.getValue())
                                .filterable(charReq.getFilterable())
                                .build())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Если не удалось распарсить JSON, оставляем пустой список
                System.err.println("Ошибка при загрузке характеристик: " + e.getMessage());
            }
        }
        builder.characteristics(characteristics);

        // Загружаем преимущества из JSON
        List<String> benefits = new ArrayList<>();
        if (product.getBenefitsJson() != null && !product.getBenefitsJson().isEmpty()) {
            try {
                benefits = objectMapper.readValue(
                        product.getBenefitsJson(),
                        new TypeReference<List<String>>() {}
                );
            } catch (Exception e) {
                // Если не удалось распарсить JSON, оставляем пустой список
                System.err.println("Ошибка при загрузке преимуществ: " + e.getMessage());
            }
        }
        builder.benefits(benefits);

        // Загружаем варианты из JSON
        List<VariantResponse> variants = new ArrayList<>();
        if (product.getVariantsJson() != null && !product.getVariantsJson().isEmpty()) {
            try {
                List<VariantRequest> variantRequests = objectMapper.readValue(
                        product.getVariantsJson(),
                        new TypeReference<List<VariantRequest>>() {}
                );
                variants = variantRequests.stream()
                        .map(vReq -> {
                            VariantResponse.VariantResponseBuilder variantBuilder = VariantResponse.builder()
                                    .variantId(vReq.getVariantId())
                                    .sku(vReq.getSku())
                                    .attributes(vReq.getAttributes() != null ? vReq.getAttributes() : new HashMap<>());
                            
                            // Преобразуем цену
                            if (vReq.getPrice() != null) {
                                variantBuilder.price(VariantResponse.PriceResponse.builder()
                                        .base(vReq.getPrice().getBase())
                                        .sale(vReq.getPrice().getSale())
                                        .currency(vReq.getPrice().getCurrency())
                                        .vat(vReq.getPrice().getVat())
                                        .build());
                            }
                            
                            // Преобразуем склад
                            if (vReq.getStock() != null) {
                                variantBuilder.stock(VariantResponse.StockResponse.builder()
                                        .quantity(vReq.getStock().getQuantity())
                                        .build());
                            }
                            
                            // Преобразуем штрихкоды
                            if (vReq.getBarcodes() != null) {
                                variantBuilder.barcodes(VariantResponse.BarcodesResponse.builder()
                                        .skuBarcode(vReq.getBarcodes().getSkuBarcode())
                                        .ean13(vReq.getBarcodes().getEan13())
                                        .build());
                            }
                            
                            // Преобразуем логистику
                            if (vReq.getLogistics() != null) {
                                VariantResponse.LogisticsResponse.LogisticsResponseBuilder logisticsBuilder = 
                                        VariantResponse.LogisticsResponse.builder()
                                                .weightKg(vReq.getLogistics().getWeightKg())
                                                .delivery(VariantResponse.LogisticsResponse.DeliveryResponse.builder()
                                                        .methods(vReq.getLogistics().getDelivery() != null 
                                                                ? vReq.getLogistics().getDelivery().getMethods() 
                                                                : new ArrayList<>())
                                                        .deliveryDays(vReq.getLogistics().getDelivery() != null 
                                                                ? vReq.getLogistics().getDelivery().getDeliveryDays() 
                                                                : null)
                                                        .build());
                                
                                if (vReq.getLogistics().getDimensionsCm() != null) {
                                    logisticsBuilder.dimensionsCm(
                                            VariantResponse.LogisticsResponse.DimensionsResponse.builder()
                                                    .length(vReq.getLogistics().getDimensionsCm().getLength())
                                                    .width(vReq.getLogistics().getDimensionsCm().getWidth())
                                                    .height(vReq.getLogistics().getDimensionsCm().getHeight())
                                                    .build()
                                    );
                                }
                                
                                variantBuilder.logistics(logisticsBuilder.build());
                            }
                            
                            return variantBuilder.build();
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Если не удалось распарсить JSON, оставляем пустой список
                System.err.println("Ошибка при загрузке вариантов: " + e.getMessage());
            }
        }
        builder.variants(variants);

        // Загружаем изображения галереи
        List<ProductImageResponse> images = new ArrayList<>();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            images = product.getImages().stream()
                    .sorted((a, b) -> Integer.compare(
                            a.getSortOrder() != null ? a.getSortOrder() : 0,
                            b.getSortOrder() != null ? b.getSortOrder() : 0))
                    .map(img -> ProductImageResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .altText(img.getAltText())
                            .sortOrder(img.getSortOrder())
                            .isPrimary(img.getIsPrimary())
                            .build())
                    .collect(Collectors.toList());
        }
        builder.images(images);

        // Добавляем информацию о категории, если она есть
        if (product.getCategory() != null) {
            builder.category(mapCategoryToResponse(product.getCategory()));
        }

        return builder.build();
    }

    private CategoryResponse mapCategoryToResponse(com.ihome24.ihome24.entity.category.Category category) {
        Long parentId = null;
        if (category.getParent() != null) {
            parentId = category.getParent().getId();
        }
        
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .isActive(category.getIsActive())
                .sortOrder(category.getSortOrder())
                .parentId(parentId)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}

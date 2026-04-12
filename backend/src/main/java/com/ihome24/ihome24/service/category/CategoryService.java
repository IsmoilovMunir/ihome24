package com.ihome24.ihome24.service.category;

import com.ihome24.ihome24.dto.request.category.CategoryRequest;
import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.entity.category.Category;
import com.ihome24.ihome24.repository.category.CategoryRepository;
import com.ihome24.ihome24.service.storage.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    public enum CategoryImageSlot {
        MAIN("category_image"),
        BANNER("category_banner"),
        MENU("category_menu"),
        COLLECTION("category_collection"),
        MOBILE("category_mobile"),
        MOBILE2("category_mobile_2"),
        MOBILE3("category_mobile_3");

        private final String baseName;

        CategoryImageSlot(String baseName) {
            this.baseName = baseName;
        }

        public String getBaseName() {
            return baseName;
        }

        public static CategoryImageSlot fromValue(String value) {
            if (value == null || value.isBlank()) return MAIN;
            try {
                return CategoryImageSlot.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Неизвестный слот изображения: " + value);
            }
        }
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Проверка на уникальность slug
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Категория с таким slug уже существует: " + request.getSlug());
        }

        Category.CategoryBuilder builder = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .bannerImageUrl(request.getBannerImageUrl())
                .menuImageUrl(request.getMenuImageUrl())
                .collectionImageUrl(request.getCollectionImageUrl())
                .mobileImageUrl(request.getMobileImageUrl())
                .mobileImageUrl2(request.getMobileImageUrl2())
                .mobileImageUrl3(request.getMobileImageUrl3())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        // Установка родительской категории, если указана
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена: " + request.getParentId()));
            builder.parent(parent);
        }

        Category savedCategory = categoryRepository.save(builder.build());
        return mapToResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        // Проверка на уникальность slug, если он изменился
        if (!category.getSlug().equals(request.getSlug()) && categoryRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Категория с таким slug уже существует: " + request.getSlug());
        }

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setBannerImageUrl(request.getBannerImageUrl());
        category.setMenuImageUrl(request.getMenuImageUrl());
        category.setCollectionImageUrl(request.getCollectionImageUrl());
        category.setMobileImageUrl(request.getMobileImageUrl());
        category.setMobileImageUrl2(request.getMobileImageUrl2());
        category.setMobileImageUrl3(request.getMobileImageUrl3());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        // Обновление родительской категории
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена: " + request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        // Проверка на наличие дочерних категорий
        if (!category.getChildren().isEmpty()) {
            throw new IllegalStateException("Невозможно удалить категорию с дочерними категориями");
        }

        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryResponse updateCategoryImage(Long id, MultipartFile file) {
        return updateCategoryImage(id, file, CategoryImageSlot.MAIN);
    }

    @Transactional
    public CategoryResponse updateCategoryImage(Long id, MultipartFile file, CategoryImageSlot slot) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));
        try {
            String imageUrl = fileService.uploadCategoryImage(file, id, slot.getBaseName());
            assignCategoryImage(category, slot, imageUrl);
            Category updatedCategory = categoryRepository.save(category);
            return mapToResponse(updatedCategory);
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось загрузить изображение категории: " + e.getMessage());
        }
    }

    @Transactional
    public CategoryResponse deleteCategoryImage(Long id) {
        return deleteCategoryImage(id, CategoryImageSlot.MAIN);
    }

    @Transactional
    public CategoryResponse deleteCategoryImage(Long id, CategoryImageSlot slot) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));
        fileService.deleteCategoryImage(id, slot.getBaseName());
        assignCategoryImage(category, slot, null);
        Category updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    private void assignCategoryImage(Category category, CategoryImageSlot slot, String imageUrl) {
        switch (slot) {
            case MAIN -> category.setImageUrl(imageUrl);
            case BANNER -> category.setBannerImageUrl(imageUrl);
            case MENU -> category.setMenuImageUrl(imageUrl);
            case COLLECTION -> category.setCollectionImageUrl(imageUrl);
            case MOBILE -> category.setMobileImageUrl(imageUrl);
            case MOBILE2 -> category.setMobileImageUrl2(imageUrl);
            case MOBILE3 -> category.setMobileImageUrl3(imageUrl);
        }
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .bannerImageUrl(category.getBannerImageUrl())
                .menuImageUrl(category.getMenuImageUrl())
                .collectionImageUrl(category.getCollectionImageUrl())
                .mobileImageUrl(category.getMobileImageUrl())
                .mobileImageUrl2(category.getMobileImageUrl2())
                .mobileImageUrl3(category.getMobileImageUrl3())
                .isActive(category.getIsActive())
                .sortOrder(category.getSortOrder())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt());

        return builder.build();
    }
}

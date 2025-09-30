package com.example.marketplace.services.impls;

import com.example.marketplace.entities.Category;
import com.example.marketplace.exceptions.AppException;
import com.example.marketplace.exceptions.CommonErrorCode;
import com.example.marketplace.repositories.CategoryRepository;
import com.example.marketplace.services.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;

    @Override
    public Category findById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Category"));
    }

    @Override
    public Category create(Category category) {
        categoryRepository.findByName(category.getName()).ifPresent(c -> {
            throw new AppException(CommonErrorCode.OBJECT_ALREADY_EXISTS, HttpStatus.CONFLICT, "Category name already exists");
        });
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Integer id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Category"));

        categoryRepository.findByName(updatedCategory.getName()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new AppException(CommonErrorCode.OBJECT_ALREADY_EXISTS, HttpStatus.CONFLICT, "Category name already exists");
            }
        });

        existingCategory.setName(updatedCategory.getName());
        return categoryRepository.save(existingCategory);
    }

    @Override
    public Page<Category> findAll(int offset, int limit) {
        return categoryRepository.findAll(PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public void delete(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Category"));

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.BAD_REQUEST, "Category has products");
        }
        categoryRepository.delete(category);
    }
}
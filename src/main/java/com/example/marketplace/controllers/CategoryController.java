package com.example.marketplace.controllers;

import com.example.marketplace.dtos.requests.categories.CategoryRequest;
import com.example.marketplace.dtos.responses.category.CategoryResponse;
import com.example.marketplace.entities.Category;
import com.example.marketplace.mappers.CategoryMapper;
import com.example.marketplace.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Category APIs")
public class CategoryController {

    CategoryService categoryService;

    CategoryMapper categoryMapper = CategoryMapper.INSTANCE;

    @Operation(summary = "Get category by ID", description = "Get category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Integer id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(categoryMapper.toCategoryResponse(category));
    }

    @Operation(summary = "Create category", description = "Create new category", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CategoryRequest request) {
        Category category = categoryMapper.toCategory(request);
        Category createdCategory = categoryService.create(category);
        return ResponseEntity.ok(categoryMapper.toCategoryResponse(createdCategory));
    }

    @Operation(summary = "Update category", description = "Update category", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Integer id,
                                                   @RequestBody @Valid CategoryRequest request) {
        Category category = categoryMapper.toCategory(request);
        Category updatedCategory = categoryService.update(id, category);
        return ResponseEntity.ok(categoryMapper.toCategoryResponse(updatedCategory));
    }

    @Operation(summary = "Get all categories", description = "Get all categories with pagination")
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> findAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<Category> categories = categoryService.findAll(offset, limit);
        Page<CategoryResponse> categoryResponses = categories.map(categoryMapper::toCategoryResponse);
        return ResponseEntity.ok(categoryResponses);
    }

    @Operation(summary = "Delete category", description = "Delete category by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.ok().build();
    }
}
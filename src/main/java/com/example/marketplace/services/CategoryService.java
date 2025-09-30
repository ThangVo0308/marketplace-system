package com.example.marketplace.services;

import com.example.marketplace.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface CategoryService {
    Category findById(Integer id);

    Category create(Category category);

    Category update(Integer id, Category category);

    Page<Category> findAll(int offset, int limit);

    void delete(Integer id);
}
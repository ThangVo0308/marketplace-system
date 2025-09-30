package com.example.marketplace.mappers;

import com.example.marketplace.dtos.requests.categories.CategoryRequest;
import com.example.marketplace.dtos.responses.category.CategoryResponse;
import com.example.marketplace.entities.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryResponse toCategoryResponse(Category entity);

    Category toCategory(CategoryRequest dto);
}
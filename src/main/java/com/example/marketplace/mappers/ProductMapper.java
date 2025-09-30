package com.example.marketplace.mappers;

import com.example.marketplace.dtos.requests.product.ProductCreateRequest;
import com.example.marketplace.dtos.requests.product.ProductUpdateRequest;
import com.example.marketplace.dtos.responses.product.ProductResponse;
import com.example.marketplace.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    Product toProduct(ProductCreateRequest request);
    Product toProduct(ProductUpdateRequest request);

    ProductResponse toProductResponse(Product product);
}

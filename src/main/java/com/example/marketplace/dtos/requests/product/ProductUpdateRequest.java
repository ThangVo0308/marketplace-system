package com.example.marketplace.dtos.requests.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 100, message = "Product name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Product description is required")
        @Size(max = 100, message = "Product description must not exceed 100 characters")
        String description,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be greater than or equal to 0")
        Integer quantity,

        @Min(value = 0, message = "Rating must be greater than or equal to 0")
        Double rating
) {}
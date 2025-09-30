package com.example.marketplace.dtos.requests.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductQuantityUpdateRequest(
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be greater than or equal to 0")
        Integer quantity
) {}
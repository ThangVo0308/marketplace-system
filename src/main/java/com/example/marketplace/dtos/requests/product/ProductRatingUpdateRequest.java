package com.example.marketplace.dtos.requests.product;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ProductRatingUpdateRequest(
        @NotNull(message = "Rating is required")
        @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
        @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
        Double rating
) {}
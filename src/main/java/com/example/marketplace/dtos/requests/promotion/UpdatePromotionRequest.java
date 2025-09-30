package com.example.marketplace.dtos.requests.promotion;

import com.example.marketplace.enums.PromotionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record UpdatePromotionRequest(
                @NotNull(message = "null_field") @NotBlank(message = "blank_field") String name,

                @NotNull(message = "null_field") @NotBlank(message = "blank_field") String description,

                @NotNull(message = "null_field") Double discountPercentage,

                @NotNull(message = "null_field") Date startDate,

                @NotNull(message = "null_field") Date endDate,

                @NotNull(message = "null_field") PromotionStatus status,

                Boolean isConfirmed) {
}
package com.example.marketplace.dtos.requests.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record BookingItemRequest(
        @NotNull(message = "null_field")
        @NotBlank(message = "blank_field")
        String orderId,

        @NotNull(message = "null_field")
        @NotBlank(message = "blank_field")
        String productID,

        @NotNull(message = "null_field")
        Double price
) {
}

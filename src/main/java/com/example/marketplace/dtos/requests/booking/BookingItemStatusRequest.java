package com.example.marketplace.dtos.requests.booking;

import com.example.marketplace.enums.BookingItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingItemStatusRequest(
        @NotNull(message = "null_field")
        @NotBlank(message = "blank_field")
        BookingItemStatus status
) {
}

package com.example.marketplace.dtos.requests.booking;

import com.example.marketplace.enums.BookingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingStatusRequest(

        @NotNull(message = "null_field")
        @NotBlank(message = "blank_field")
        BookingStatus status
)
{ }

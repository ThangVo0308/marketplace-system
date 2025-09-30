package com.example.marketplace.dtos.requests.booking;

import com.example.marketplace.enums.BookingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record BookingRequest(

        @NotNull(message = "null_field")
        @NotBlank(message = "blank_field")
        String userId,

        @NotNull(message = "null_field")
        BookingStatus status
)
{ }

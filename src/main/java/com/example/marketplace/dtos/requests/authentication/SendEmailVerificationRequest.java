package com.example.marketplace.dtos.requests.authentication;

import com.example.marketplace.enums.VerificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendEmailVerificationRequest (

        @NotNull(message = "null_field")
        @NotBlank(message = "blank_field")
        @Email(message = "invalid_email")
        String email,

        @NotNull(message = "null_field")
        VerificationType type

) {
}

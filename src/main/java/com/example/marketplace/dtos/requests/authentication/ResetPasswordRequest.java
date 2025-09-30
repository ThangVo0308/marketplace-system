package com.example.marketplace.dtos.requests.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest (

    @NotNull(message = "null_field")
    @NotBlank(message = "blank_field")
    String token,

    @NotNull(message = "null_field")
    @NotBlank(message = "blank_field")
    @Size(min = 6, max = 20, message = "size_field")
    String password,

    @NotNull(message = "null_field")
    @NotBlank(message = "blank_field")
    @Size(min = 6, max = 20, message = "size_field")
    String passwordConfirmation

) {

}
package com.example.marketplace.dtos.requests.user;

import com.example.marketplace.enums.Gender;
import com.example.marketplace.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserUpdateRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,

        @Size(max = 15, message = "Mobile number must not exceed 15 characters")
        String mobileNumber,

        LocalDate birthdate,

        Gender gender,

        String avatar,

        UserStatus userStatus
) {}
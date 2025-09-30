package com.example.marketplace.dtos.requests.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleRequest {

    @NotBlank(message = "Role name cannot be empty")
    @Size(max = 100, message = "Role name must be less than 100 characters")
    String name;
}
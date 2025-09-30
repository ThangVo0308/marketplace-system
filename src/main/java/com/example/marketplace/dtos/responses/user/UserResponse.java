package com.example.marketplace.dtos.responses.user;

import com.example.marketplace.enums.Gender;
import com.example.marketplace.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    String id;

    String email;

    String username;

    String firstName;

    String lastName;

    String middleName;

    String mobileNumber;

    String password;

    LocalDate birthdate;

    @JsonProperty("avatar")
    String mAvatar;

    @JsonProperty("role")
    String mRole;

    Gender gender;

    UserStatus status;
}
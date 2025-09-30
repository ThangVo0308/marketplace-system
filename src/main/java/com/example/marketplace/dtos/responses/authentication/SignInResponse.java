package com.example.marketplace.dtos.responses.authentication;

import com.example.marketplace.dtos.responses.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInResponse {

    TokensResponse tokensResponse;

    UserResponse userInfo;

}
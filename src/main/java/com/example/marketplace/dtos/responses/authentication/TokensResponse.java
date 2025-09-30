package com.example.marketplace.dtos.responses.authentication;

public record TokensResponse(

    String accessToken,
    String refreshToken

) {
}

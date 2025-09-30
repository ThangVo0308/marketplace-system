package com.example.marketplace.dtos.responses.authentication;

public record SendEmailForgotPasswordResponse(

    String message,

    int retryAfter

) {

}
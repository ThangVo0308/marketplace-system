package com.example.marketplace.dtos.responses.booking;

public record VNPayResponse(
        String code,
        String message,
        String paymentUrl
) {
}

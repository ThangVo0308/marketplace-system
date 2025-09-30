package com.example.marketplace.dtos.responses.product;

import com.example.marketplace.dtos.responses.user.UserResponse;

import java.util.Date;

public record ProductResponse(
        String id,
        String name,
        String description,
        Integer quantity,
        Double rating,
        UserResponse user,
        Date createdAt,
        Date updatedAt
) {}
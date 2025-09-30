package com.example.marketplace.dtos.responses.booking;

import com.example.marketplace.dtos.responses.product.ProductResponse;
import com.example.marketplace.enums.BookingItemStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingItemResponse {

    String id;

    Date availableDate;

    Date startTime;

    Date endTime;

    Date createdAt;

    BookingItemStatus status;

    double price;

    @JsonProperty(value = "createdBy")
    String createdBy;

    @JsonProperty(value = "product")
    ProductResponse mProduct;
}
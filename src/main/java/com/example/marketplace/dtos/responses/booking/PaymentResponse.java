package com.example.marketplace.dtos.responses.booking;

import com.example.marketplace.enums.PaymentMethod;
import com.example.marketplace.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {

    String id;

    PaymentMethod method;

    Double price;

    PaymentStatus status;

    @JsonProperty(value = "booking")
    BookingResponse mBooking;
}

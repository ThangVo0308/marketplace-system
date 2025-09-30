package com.example.marketplace.dtos.responses.booking;

import com.example.marketplace.dtos.responses.user.UserResponse;
import com.example.marketplace.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    String id;

    Date orderDate;

    Date createdAt;

    @JsonIgnore
    BookingStatus status;

    @JsonProperty(value = "user")
    UserResponse mUser;

    @JsonProperty(value = "bookingItems")
    List<BookingItemResponse> mBookingItems;

}
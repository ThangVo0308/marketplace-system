package com.example.marketplace.exceptions.booking;

import lombok.Getter;

@Getter
public enum BookingErrorCode {

    FIELD_AVAILABILITY_ORDERED("booking/availability-ordered", "field_availability_has_been_ordered"),

    PRODUCT_NOT_AVAILABLE("booking/not-available","product_not_available"),

    BOOKING_CHECK_PENDING("booking/check-pending", "booking_check_pending"),

    USER_BANNED("booking/user-banned", "user_is_banned"),

    BOOKING_FAILED("booking/failed", "booking_failed"),

    CANCEL_FAILED("booking/cancel-failed", "cancel_failed"),
    ;

    BookingErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private final String code;
    private final String message;

}

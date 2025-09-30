package com.example.marketplace.exceptions.booking;

import com.example.marketplace.exceptions.AppException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BookingException extends AppException {

    public BookingException(BookingErrorCode bookingErrorCode, HttpStatus httpStatus) {
        super(bookingErrorCode.getMessage(), httpStatus);
        this.bookingErrorCode = bookingErrorCode;
    }

    private final BookingErrorCode bookingErrorCode;
}

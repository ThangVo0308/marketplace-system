package com.example.marketplace.exceptions.payment;

import com.example.marketplace.exceptions.AppException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentException extends AppException {

    public PaymentException(PaymentErrorCode paymentErrorCode, HttpStatus httpStatus) {
        super(paymentErrorCode.getMessage(), httpStatus);
        this.paymentErrorCode = paymentErrorCode;
    }

    private final PaymentErrorCode paymentErrorCode;

}
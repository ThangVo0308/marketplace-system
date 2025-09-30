package com.example.marketplace.services;

import com.example.marketplace.dtos.responses.booking.VNPayResponse;
import com.example.marketplace.entities.Payment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface PaymentService {

    Payment findById(String id);

    VNPayResponse createVNPayPayment(long amount, String orderId, HttpServletRequest request);
    boolean verifyVNPayPayment(Map<String, String> params, String orderId, String secureHash);

    Payment create(double amount, String orderId);

}

package com.example.marketplace.controllers;

import com.example.marketplace.dtos.requests.payment.PaymentRequest;
import com.example.marketplace.dtos.requests.payment.VNPayRequest;
import com.example.marketplace.dtos.responses.booking.PaymentResponse;
import com.example.marketplace.dtos.responses.booking.VNPayResponse;
import com.example.marketplace.entities.BookingItem;
import com.example.marketplace.mappers.BookingItemMapper;
import com.example.marketplace.mappers.PaymentMapper;
import com.example.marketplace.services.BookingItemsService;
import com.example.marketplace.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Payment APIs")
public class PaymentController {

    PaymentService paymentService;

    BookingItemsService bookingItemsService;

    BookingItemMapper bookingItemMapper = BookingItemMapper.INSTANCE;

    PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    // Card number: 9704198526191432198
    // Owner name: NGUYEN VAN A
    // Date: 07/15
    // Bank code: Base on card's type in API card:
    // https://sandbox.vnpayment.vn/apis/vnpay-demo
    @Operation(summary = "Create VNPay Payment", description = "Create VNPay Payment")
    @PostMapping("/vnpay")
    public ResponseEntity<VNPayResponse> createVNPayPayment(@RequestBody @Valid VNPayRequest payRequest,
                                                            HttpServletRequest request) {
        long amount = payRequest.amount();
        String orderId = payRequest.orderId();

        VNPayResponse vnPayResponse = paymentService.createVNPayPayment(amount, orderId, request);

        return ResponseEntity.ok(vnPayResponse);
    }


    @Operation(summary = "Create Payment", description = "Create Payment")
    @PostMapping
    public ResponseEntity<PaymentResponse> create(@RequestBody @Valid PaymentRequest request) {
        double amount = request.amount();
        String orderId = request.orderId();
        return ResponseEntity.ok(paymentMapper.toPaymentResponse(paymentService.create(amount, orderId)));
    }

    @Operation(summary = "VNPay Callback", description = "Handle VNPay payment callback")
    @GetMapping("/vnpay/callback")
    public ResponseEntity<Void> vnPayCallback(@RequestParam Map<String, String> params, HttpServletRequest request) {
        log.info("VNPay Callback received with params: {}", params);

        String orderId = params.get("vnp_TxnRef");
        String secureHash = params.get("vnp_SecureHash");

        boolean isVerified = paymentService.verifyVNPayPayment(params, orderId, secureHash);

        String status = isVerified ? "success" : "failed";
        request.getSession().setAttribute("paymentStatus", status);

        String redirectUrl = "http://localhost:3333/marketplace/?paymentStatus=" + status;
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }


    @Operation(summary = "Get product price & bookingId", description = "Get product price & bookingId when user clicks on payment form", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/payment-info/{bookingId}")
    public ResponseEntity<Map<String, Object>> getPaymentInfo(@PathVariable String bookingItemsID) {
        BookingItem bookingItem = bookingItemsService.findById(bookingItemsID);
        double totalPrice = bookingItem.getPrice();
        return ResponseEntity.ok(
                Map.of("totalPrice", totalPrice, "bookingId", bookingItemMapper.toBookingItemResponse(bookingItem)));
    }

}

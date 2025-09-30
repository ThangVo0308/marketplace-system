package com.example.marketplace.services.impls;

import com.example.marketplace.configs.VNPayConfig;
import com.example.marketplace.dtos.responses.booking.VNPayResponse;
import com.example.marketplace.entities.Booking;
import com.example.marketplace.entities.Payment;
import com.example.marketplace.enums.BookingStatus;
import com.example.marketplace.enums.PaymentMethod;
import com.example.marketplace.enums.PaymentStatus;
import com.example.marketplace.exceptions.AppException;
import com.example.marketplace.exceptions.CommonErrorCode;
import com.example.marketplace.exceptions.booking.BookingErrorCode;
import com.example.marketplace.exceptions.booking.BookingException;
import com.example.marketplace.exceptions.payment.PaymentErrorCode;
import com.example.marketplace.exceptions.payment.PaymentException;
import com.example.marketplace.helpers.VNPayUtils;
import com.example.marketplace.services.BookingService;
import com.example.marketplace.services.PaymentRepository;
import com.example.marketplace.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.marketplace.components.Translator.getLocalizedMessage;


@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    VNPayConfig vnPayConfig;

    BookingService bookingService;

    PaymentRepository paymentRepository;

    @Override
    public Payment findById(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Payment"));
    }

    @Override
    public VNPayResponse createVNPayPayment(long amount, String orderId, HttpServletRequest request) {
        long vnpAmount = amount * 100L;

        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(vnpAmount));

        vnpParamsMap.put("vnp_TxnRef", orderId);

        vnpParamsMap.put("vnp_IpAddr", VNPayUtils.getIpAddress(request));

        String queryUrl = VNPayUtils.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtils.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return new VNPayResponse("ok", getLocalizedMessage("payment_success"), paymentUrl);
    }

    public static String generateRawHash(String accessKey, String amount, String extraData, String ipnUrl,
                                         String orderId, String orderInfo, String partnerCode,
                                         String redirectUrl, String requestId, String requestType) {
        // Build data string in sorted order
        String data = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
        return data;
    }

    @Override
    public boolean verifyVNPayPayment(Map<String, String> params, String orderId, String secureHash) {
        // Filter out the vnp_SecureHash parameter
        Map<String, String> filteredParams = params.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("vnp_SecureHash"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Recalculate the hash
        String calculatedHash = VNPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), VNPayUtils.getPaymentURL(filteredParams, false));

        // Compare secure hashes
        if (!calculatedHash.equals(secureHash)) {
            return false;
        }

        // Get payment status from params
        String paymentStatus = params.get("vnp_ResponseCode");
        Double price = Double.parseDouble(params.get("vnp_Amount")) / 100;

        // Fetch the order using the orderId
        Booking booking = bookingService.findById(orderId);

        PaymentStatus paymentStatusEnum = "00".equals(paymentStatus) ? PaymentStatus.COMPLETED : PaymentStatus.PENDING;

        Payment payment = Payment.builder()
                .method(PaymentMethod.VN_PAY)
                .price(price)
                .booking(booking)
                .status(paymentStatusEnum)
                .createdBy(booking.getUser().getId())
                .build();

        paymentRepository.save(payment);

        paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Payment"));

        return "00".equals(paymentStatus);
    }

    @Override
    public Payment create(double amount, String orderId) {
        Booking booking = bookingService.findById(orderId);

        if (amount <= 0) throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_INVALID, HttpStatus.BAD_REQUEST);
        if (booking == null) throw new PaymentException(PaymentErrorCode.PAYMENT_ORDER_NOT_FOUND, HttpStatus.NOT_FOUND);
        if (booking.getStatus() != BookingStatus.PENDING) throw new BookingException(BookingErrorCode.BOOKING_CHECK_PENDING, HttpStatus.BAD_REQUEST);

        Payment payment = Payment.builder()
                .price(amount)
                .booking(booking)
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .createdBy(booking.getUser().getId())
                .build();

        return paymentRepository.save(payment);
    }


}

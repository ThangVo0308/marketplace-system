package com.example.marketplace.dtos.requests.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoOneTimePaymentRequest {

    String partnerCode;

    String subPartnerCode;

    String partnerName;

    String storeId;

    String requestId;

    String amount;

    String orderId;

    String orderInfo;

    String orderGroupId;

    String redirectUrl;

    String ipnUrl;

    String requestType;

    String extraData;

    String autoCapture;

    String lang;

    String signature;

    String referenceId;

    UserInfo userInfo;

    List<Item> items;

    public MomoOneTimePaymentRequest(String orderId, String orderInfo, String lang, UserInfo userInfo, List<Item> items) {
        this.orderId = orderId;
        this.orderInfo = orderInfo;
        this.lang = lang;
        this.userInfo = userInfo;
        this.items = items;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserInfo {

        String name;

        String phoneNumber;

        String email;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Item {

        String id;

        String name;

        String description;

        String category;

        String imageUrl;

        String manufacturer;

        int price;

        int quantity;

        String currency;

        String unit;

        int totalPrice;

        String taxAmount;

    }
}


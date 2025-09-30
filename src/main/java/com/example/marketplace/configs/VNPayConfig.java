package com.example.marketplace.configs;

import com.example.marketplace.helpers.VNPayUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Configuration
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayConfig {
    @Getter
    @Value("${payment.vnPay.url}")
    String vnp_PayUrl;

    @Getter
    @Value("${payment.vnPay.returnUrl}")
    String vnp_ReturnUrl;

    @Value("${payment.vnPay.tmnCode}")
    String vnp_TmnCode ;

    @Getter
    @Value("${payment.vnPay.secretKey}")
    String secretKey;

    @Value("${payment.vnPay.version}")
    String vnp_Version;

    @Value("${payment.vnPay.command}")
    String vnp_Command;

    @Value("${payment.vnPay.orderType}")
    String orderType;

    public Map<String, String> getVNPayConfig() {
        Map<String, String> vnpParamsMap = new HashMap<>();

        vnpParamsMap.put("vnp_Version", this.vnp_Version);
        vnpParamsMap.put("vnp_Command", this.vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", this.vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_TxnRef",  VNPayUtils.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toán đơn hàng:" +  VNPayUtils.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderType", this.orderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        vnpParamsMap.put("vnp_ReturnUrl", this.vnp_ReturnUrl);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        // start time
        String vnpCreateDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_CreateDate", vnpCreateDate);
        calendar.add(Calendar.MINUTE, 15);

        // end time
        String vnpExpireDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_ExpireDate",vnpExpireDate);

        return vnpParamsMap;
    }

}

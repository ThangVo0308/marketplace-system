package com.example.marketplace.components;

import com.example.marketplace.annotations.RateLimit;
import com.example.marketplace.enums.RateLimitKeyType;
import com.example.marketplace.exceptions.authenication.AuthenticationErrorCode;
import com.example.marketplace.exceptions.authenication.AuthenticationException;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class RateLimitAspect {
    private final HttpServletRequest request;
    public RateLimitAspect(HttpServletRequest request) {
        this.request = request;
    }

    private Map<String, Map<String, AtomicInteger>> requestCounts = new ConcurrentHashMap<>();

    @Before("annotation(rateLimit) && execution(* *(..))")
    public void rateLimit(RateLimit rateLimit) throws ParseException {
        int limit = rateLimit.limit();
        int timeLimit = rateLimit.timeLimit();
        RateLimitKeyType[] keysType = rateLimit.keysType();

        for (RateLimitKeyType keyType : keysType) {
            String key = generateKeyInfo(keyType);

            requestCounts.computeIfAbsent(
                    key,
                    k -> new ConcurrentHashMap<>()); // new user calls API

            Map<String, AtomicInteger> map = requestCounts.get(key);

            // Check if the requested count has reached the limit bases on IP/Token
            AtomicInteger count = map.computeIfAbsent(
                    String.valueOf(System.currentTimeMillis()),
                    k -> new AtomicInteger(0)); // if key does not exist >> initialize count by 0

            if (count.incrementAndGet() > limit) {
                if (keyType == RateLimitKeyType.BY_TOKEN) {
                    throw new AuthenticationException(AuthenticationErrorCode.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
                }
                throw new AuthenticationException(
                        AuthenticationErrorCode.TOO_MANY_REQUESTS,
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }
        }
    }

    private String generateKeyInfo(RateLimitKeyType keyType) throws ParseException {
        switch(keyType) {
            case BY_IP -> {
                String ipAddress = request.getRemoteAddr();
                String xForwardedForHeader = request.getHeader("X-Forwarded-For");

                if (xForwardedForHeader != null) {
                    ipAddress = xForwardedForHeader.split(",")[0];
                }
                return ipAddress;
            }

            default -> {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    SignedJWT signedJWT = SignedJWT.parse(token);
                    return signedJWT.getJWTClaimsSet().getJWTID();
                } else {
                    throw new AuthenticationException(AuthenticationErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
                }
            }
        }
    }
}

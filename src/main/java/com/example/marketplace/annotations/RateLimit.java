package com.example.marketplace.annotations;

import com.example.marketplace.enums.RateLimitKeyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int limit() default 5;

    int timeLimit() default 60;

    RateLimitKeyType[] keysType() default { RateLimitKeyType.BY_IP };
}

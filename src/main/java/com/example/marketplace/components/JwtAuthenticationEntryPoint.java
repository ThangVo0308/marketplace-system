package com.example.marketplace.components;

import com.example.marketplace.dtos.responses.other.CommonResponse;
import com.example.marketplace.exceptions.authenication.AuthenticationErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static com.example.marketplace.components.Translator.getLocalizedMessage;

// This class is used to handle the exception when the user is not authenticated
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        AuthenticationErrorCode authenticationErrorCode = AuthenticationErrorCode.TOKEN_MISSING;

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        CommonResponse<?, ?> commonResponse = CommonResponse.builder()
                .errorCode(authenticationErrorCode.getCode())
                .message(getLocalizedMessage(authenticationErrorCode.getMessage()))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        log.error("Unauthorized error: {}", authException.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(commonResponse));
        response.flushBuffer();
    }

}
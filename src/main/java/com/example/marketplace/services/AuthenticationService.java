package com.example.marketplace.services;

import com.example.marketplace.entities.User;
import com.example.marketplace.enums.ProviderType;
import com.example.marketplace.enums.VerificationType;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;

@Service
public interface AuthenticationService {
    boolean introspect(String token) throws JOSEException, ParseException;

    void signUp(User user, String confirmationPassword, boolean acceptTerms, boolean isSeller);

    void sendEmailVerification(String email, VerificationType verificationType);

    void verifyEmail(User user, String code, String token);

    User signIn(String email, String password);

    String generateSocialAuthUrl(ProviderType providerType);

    Map<String, Object> fetchSocialUser(String code, ProviderType providerType) throws Exception;

    String generateToken(User user, boolean isRefresh);

    User refresh(String refreshToken, HttpServletRequest servletRequest) throws ParseException, JOSEException;

    void sendEmailForgotPassword(String email);

    String forgotPassword(User user, String code);

    void resetPassword(String token, String password, String confirmationPassword);

    void signOut(String accessToken, String refreshToken) throws ParseException, JOSEException;
}

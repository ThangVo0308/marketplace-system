package com.example.marketplace.controllers;

import com.example.marketplace.annotations.RateLimit;
import com.example.marketplace.dtos.requests.authentication.*;
import com.example.marketplace.dtos.responses.authentication.*;
import com.example.marketplace.entities.User;
import com.example.marketplace.enums.Gender;
import com.example.marketplace.enums.ProviderType;
import com.example.marketplace.enums.UserStatus;
import com.example.marketplace.exceptions.authenication.AuthenticationException;
import com.example.marketplace.mappers.UserMapper;
import com.example.marketplace.services.AuthenticationService;
import com.example.marketplace.services.UserService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Map;

import static com.example.marketplace.enums.RateLimitKeyType.BY_IP;
import static com.example.marketplace.enums.RateLimitKeyType.BY_TOKEN;
import static com.example.marketplace.exceptions.authenication.AuthenticationErrorCode.INVALID_SIGNATURE;
import static com.example.marketplace.exceptions.authenication.AuthenticationErrorCode.PROVIDER_NOT_SUPPORTED;
import static org.springframework.http.HttpStatus.*;
import static com.example.marketplace.enums.VerificationType.*;
import static com.example.marketplace.components.Translator.getLocalizedMessage;



@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Authentication APIs")
public class AuthenticationController {
    AuthenticationService authenticationService;

    UserService userService;

    UserMapper userMapper = UserMapper.INSTANCE;

    @Operation(summary = "Sign up", description = "Create new user")
    @PostMapping("/sign-up")
    @ResponseStatus(CREATED)
    ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        User user = userMapper.toUser(request);
        authenticationService.signUp(user, request.passwordConfirmation(), request.acceptTerms(), request.isSeller());
        authenticationService.sendEmailVerification(request.email(), VERIFY_EMAIL_BY_TOKEN);

        return ResponseEntity.status(CREATED).body(
               new SignUpResponse(getLocalizedMessage("sign_up_success"), user.getId())
        );
    }

    @Operation(summary = "Send email verification", description = "Send email verification")
    @PostMapping("/send-email-verification")
    @ResponseStatus(OK)
    ResponseEntity<SendEmailVerificationResponse> sendEmailVerification(@RequestBody @Valid SendEmailVerificationRequest request) {
        authenticationService.sendEmailVerification(request.email(), request.type());

        return ResponseEntity.status(OK).body(
                new SendEmailVerificationResponse(getLocalizedMessage("resend_verification_email_success"))
        );
    }

    @Operation(summary = "Verify email by code", description = "Verify email by code")
    @PostMapping("/verify-email-by-code")
    @ResponseStatus(OK)
    ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestBody @Valid VerifyEmailByCodeRequest request) {
        User user = userService.findByEmail(request.email());

        authenticationService.verifyEmail(user, request.email(), request.code());

        return ResponseEntity.status(OK).body(
                new VerifyEmailResponse(getLocalizedMessage("verify_email_success"))
        );
    }

    @Operation(summary = "Verify email by token", description = "Verify email by token")
    @PostMapping("/verify-email-by-token")
    @ResponseStatus(OK)
    ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam(name = "token") String token) {
        authenticationService.verifyEmail(null, null, token);

        return ResponseEntity.status(OK).body(
                new VerifyEmailResponse(getLocalizedMessage("verify_email_success"))
        );
    }

    @Operation(summary = "Sign in", description = "Sign in")
    @PostMapping("/sign-in")
    @ResponseStatus(OK)
    @RateLimit(keysType = { BY_IP })
    ResponseEntity<SignInResponse> signIn(@RequestBody @Valid SignInRequest request) {
        User user = userService.findByEmail(request.email());

        authenticationService.signIn(request.email(), request.password());

        String accessToken = authenticationService.generateToken(user, true);
        String refreshToken = authenticationService.generateToken(user, false);

        return ResponseEntity.status(OK).body(
                SignInResponse.builder()
                        .tokensResponse(new TokensResponse(accessToken, refreshToken))
                        .userInfo(userMapper.toUserResponse(user))
                        .build()
        );
    }

    @Operation(summary = "Sign in with social", description = "Sign in with social")
    @PostMapping("/social")
    @ResponseStatus(OK)
    @RateLimit(keysType = { BY_IP })
    ResponseEntity<SocialSignInResponse> signInWithSocial(@RequestParam(defaultValue = "google") String provider) {
        ProviderType type;
        try {
            type = ProviderType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException(PROVIDER_NOT_SUPPORTED, UNPROCESSABLE_ENTITY);
        }
        String url = authenticationService.generateSocialAuthUrl(type);
        return ResponseEntity.status(OK).body(new SocialSignInResponse(url));
    }

    @Operation(summary = "Verify forgot password code", description = "Verify forgot password code")
    @PostMapping("/forgot")
    @ResponseStatus(OK)
    ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        User user = userService.findByEmail(request.email());
        String forgotPasswordToken = authenticationService.forgotPassword(user, request.code());

        return ResponseEntity.status(OK).body(new ForgotPasswordResponse(
                getLocalizedMessage("verify_forgot_password_code_success"),
                forgotPasswordToken));
    }

    @Operation(summary = "Reset password", description = "Reset password")
    @PostMapping("/reset")
    @ResponseStatus(OK)
    ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request.token(), request.password(), request.passwordConfirmation());

        return ResponseEntity.status(OK).body(
                new ResetPasswordResponse(getLocalizedMessage("reset_password_success")));
    }

    @Operation(summary = "Introspect", description = "Introspect provided token")
    @PostMapping("/introspect")
    @ResponseStatus(OK)
    ResponseEntity<IntrospectResponse> introspect(@RequestBody @Valid IntrospectRequest request)
            throws ParseException, JOSEException {
        boolean isValid = authenticationService.introspect(request.token());

        return ResponseEntity.status(OK).body(new IntrospectResponse(isValid));
    }

    @Operation(summary = "Send email forgot password", description = "Send email forgot password")
    @PostMapping("/send-forgot-password")
    @ResponseStatus(OK)
    @RateLimit(keysType = { BY_IP })
    ResponseEntity<SendEmailForgotPasswordResponse> sendEmailForgotPassword(
            @RequestBody @Valid SendEmailForgotPasswordRequest request) {
        authenticationService.sendEmailForgotPassword(request.email());

        return ResponseEntity.status(OK).body(new SendEmailForgotPasswordResponse(
                getLocalizedMessage("send_forgot_password_email_success"),
                60));
    }

    @Operation(summary = "Sign out", description = "Sign out")
    @PostMapping("/sign-out")
    @ResponseStatus(OK)
    void signOut(@RequestBody @Valid SignOutRequest request) {
        String accessToken = request.accessToken();
        String refreshToken = request.refreshToken();

        try {
            authenticationService.signOut(accessToken, refreshToken);
        } catch (ParseException | JOSEException e) {
            throw new AuthenticationException(INVALID_SIGNATURE, UNPROCESSABLE_ENTITY);
        }
    }

    @Operation(summary = "Social callback", description = "Social callback")
    @GetMapping("/social/callback")
    @ResponseStatus(OK)
    @RateLimit(keysType = { BY_IP })
    ResponseEntity<SignInResponse> socialCallBack(@RequestParam String code, @RequestParam String provider) {
        ProviderType type;
        Map<String, Object> userInfo;

        try {
            type = ProviderType.valueOf(provider.toUpperCase());
            userInfo = authenticationService.fetchSocialUser(code, type);
        } catch(Exception e) {
            throw new AuthenticationException(PROVIDER_NOT_SUPPORTED, UNPROCESSABLE_ENTITY);
        }

        String email = userInfo.get("email").toString();
        String name = userInfo.get("name").toString();
        String randomPassword = "$2y$10$ZGmN.PsOe1FqAy8Sp4IkrOQ7MeIMV01iDt6TLNbiLPTSaJLWUeP/C";
        if (!userService.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .username(name)
                    .firstName(name.split(" ")[0])
                    .lastName(name.split(" ")[1])
                    .mobileNumber("not provided")
                    .gender(Gender.Other)
                    .birthdate(LocalDate.now().minusYears(22))
                    .password(randomPassword)
                    .isActivated(true)
                    .userStatus(UserStatus.ACTIVE)
                    .build();

            authenticationService.signUp(user, randomPassword, true, false);
        }

        User signInUser = authenticationService.signIn(email, randomPassword);

        String accessToken = authenticationService.generateToken(signInUser, false);
        String refreshToken = authenticationService.generateToken(signInUser, true);

        return ResponseEntity.status(OK).body(
                SignInResponse.builder()
                        .tokensResponse(new TokensResponse(accessToken, refreshToken))
                        .userInfo(userMapper.toUserResponse(signInUser)).build());
    }

    @Operation(summary = "Refresh", description = "Refresh token")
    @PostMapping("/refresh")
    @ResponseStatus(OK)
    @RateLimit(keysType = { BY_TOKEN })
    ResponseEntity<RefreshResponse> refresh(@RequestParam @Valid RefreshRequest request, HttpServletRequest httpServletRequest) {
        User user;

        try {
            user = authenticationService.refresh(request.refreshToken(), httpServletRequest);
        } catch (ParseException | JOSEException e) {
            throw new AuthenticationException(INVALID_SIGNATURE, UNPROCESSABLE_ENTITY);
        }

        String newAccessToken = authenticationService.generateToken(user, false);

        return ResponseEntity.status(OK).body(new RefreshResponse(
                getLocalizedMessage("refresh_token_success"),
                newAccessToken));
    }

}

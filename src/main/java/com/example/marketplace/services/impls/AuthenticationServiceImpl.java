package com.example.marketplace.services.impls;

import com.example.marketplace.dtos.requests.GoogleAuthorizationCodeTokenRequest;
import com.example.marketplace.entities.User;
import com.example.marketplace.entities.Verification;
import com.example.marketplace.enums.ProviderType;
import com.example.marketplace.enums.UserStatus;
import com.example.marketplace.enums.VerificationType;
import com.example.marketplace.repositories.VerificationRepository;
import com.example.marketplace.services.AuthenticationService;
import com.example.marketplace.services.BaseRedisService;
import com.example.marketplace.services.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.example.marketplace.exceptions.authenication.AuthenticationException;

import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

import static com.example.marketplace.enums.VerificationType.*;
import static com.example.marketplace.exceptions.authenication.AuthenticationErrorCode.*;
import static com.example.marketplace.helpers.Constants.*;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    UserService userService;

//    RoleService roleService;
//
//    UserRoleService userRoleService;

    VerificationRepository verificationRepository;

    PasswordEncoder passwordEncoder;

    KafkaTemplate<String, String> kafkaTemplate;

//    BaseRedisService<String, String, Object> baseRedisService;

    @NonFinal
    @Value("${security.oauth2.client.registration.google.client-id}")
    String GOOGLE_CLIENT_ID;

    @NonFinal
    @Value("${security.oauth2.client.registration.google.client-secret}")
    String GOOGLE_CLIENT_SECRET;

    @NonFinal
    @Value("${security.oauth2.client.registration.google.redirect-uri}")
    String GOOGLE_REDIRECT_URI;

    @NonFinal
    @Value("${security.oauth2.client.registration.google.auth-uri}")
    String GOOGLE_AUTH_URI;

    @NonFinal
    @Value("${security.oauth2.client.registration.google.token-uri}")
    String GOOGLE_TOKEN_URI;

    @NonFinal
    @Value("${security.oauth2.client.registration.google.user-info-uri}")
    String GOOGLE_USER_INFO_URI;

    @NonFinal
    @Value("${security.oauth2.client.registration.google.scope}")
    String GOOGLE_SCOPE;

    @NonFinal
    @Value("${security.oauth2.client.registration.facebook.client-id}")
    String FACEBOOK_CLIENT_ID;

    @NonFinal
    @Value("${security.oauth2.client.registration.facebook.client-secret}")
    String FACEBOOK_CLIENT_SECRET;

    @NonFinal
    @Value("${security.oauth2.client.registration.facebook.redirect-uri}")
    String FACEBOOK_REDIRECT_URI;

    @NonFinal
    @Value("${security.oauth2.client.registration.facebook.auth-uri}")
    String FACEBOOK_AUTH_URI;

    @NonFinal
    @Value("${security.oauth2.client.registration.facebook.token-uri}")
    String FACEBOOK_TOKEN_URI;

    @NonFinal
    @Value("${security.oauth2.client.registration.facebook.user-info-uri}")
    String FACEBOOK_USER_INFO_URI;

    @NonFinal
    @Value("${security.oauth2.client.registration.facebook.scope}")
    String FACEBOOK_SCOPE;

    @NonFinal
    @Value("${jwt.accessSignerKey}")
    protected String ACCESS_SIGNER_KEY;

    @NonFinal
    @Value("${jwt.refreshSignerKey}")
    protected String REFRESH_SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Override
    public boolean introspect(String token) throws JOSEException, ParseException {
        boolean isValid = true;

        try {
            verifyToken(token, false);

        } catch (AuthenticationException e) {
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void signUp(User user, String confirmationPassword, boolean acceptTerms, boolean isSeller) {
        if (userService.existsByEmail(user.getEmail()))
            throw new AuthenticationException(EMAIL_ALREADY_IN_USE, CONFLICT);

        if (!user.getPassword().equals(confirmationPassword))
            throw new AuthenticationException(PASSWORD_MIS_MATCH, BAD_REQUEST);

        if (isInvalidEmail(user.getEmail()))
            throw new AuthenticationException(INVALID_EMAIL, BAD_REQUEST);

        if (isWeakPassword(user.getPassword()))
            throw new AuthenticationException(WEAK_PASSWORD, BAD_REQUEST);

        if (!acceptTerms)
            throw new AuthenticationException(TERMS_NOT_ACCEPTED, BAD_REQUEST);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserStatus(UserStatus.ACTIVE);

        try {
            User newUser = userService.create(user);
//            if (isSeller) {
//                userRoleService.create(UserRole.builder()
//                        .user(newUser)
//                        .role(roleService.findByName("FIELD_OWNER"))
//                        .build());
//            } else {
//                userRoleService.create(UserRole.builder()
//                        .user(newUser)
//                        .role(roleService.findByName("USER"))
//                        .build());
//            }

        } catch (DataIntegrityViolationException exception) {
            throw new AuthenticationException(CREATE_USER_FAILED, CONFLICT);
        }
    }

    @Override
    @Transactional
    public void sendEmailVerification(String email, VerificationType verificationType) {
        User user = userService.findByEmail(email);

        List<Verification> verifications = verificationRepository.findByUserAndVerificationType(user, verificationType);

        if (verificationType.equals(VERIFY_EMAIL_BY_CODE) || verificationType.equals(VERIFY_EMAIL_BY_TOKEN)) {
            if (user.isActivated())
                throw new AuthenticationException(USER_ALREADY_VERIFIED, BAD_REQUEST);

            else {
                if (!verifications.isEmpty()) verificationRepository.deleteAll(verifications);

                sendEmail(email, verificationType);
            }

        } else {
            if (verifications.isEmpty()) throw new AuthenticationException(CANNOT_SEND_EMAIL, BAD_REQUEST);

            else {
                verificationRepository.deleteAll(verifications);
                sendEmail(email, verificationType);
            }
        }
    }

    @Override
    @Transactional
    public void verifyEmail(User user, String code, String token) {
        Verification verification = (code != null)
                ? verificationRepository.findByCode(code).orElseThrow(() ->
                new AuthenticationException(CODE_INVALID, BAD_REQUEST))

                : verificationRepository.findById(token).orElseThrow(() ->
                new AuthenticationException(CODE_INVALID, BAD_REQUEST));

        if (verification.getExpiryTime().before(new Date()))
            throw new AuthenticationException(CODE_INVALID, UNPROCESSABLE_ENTITY);

        userService.activateUser((user != null) ? user : verification.getUser());

        verificationRepository.delete(verification);
    }

    @Override
    public User signIn(String email, String password) {
        User user = userService.findByEmail(email);

        if (isPasswordExpired(user))
            throw new AuthenticationException(EXPIRED_PASSWORD, CONFLICT);

        if (isTwoFactorRequired(user))
            throw new AuthenticationException(TWO_FACTOR_REQUIRED, FORBIDDEN);

        if (isUserDisabled(user))
            throw new AuthenticationException(USER_DISABLED, FORBIDDEN);

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new AuthenticationException(WRONG_PASSWORD, UNAUTHORIZED);

        if (user.getUserStatus() == UserStatus.BANNED) {
            throw new AuthenticationException(USER_BANNED, CONFLICT);
        }

        if (!user.isActivated()) throw new AuthenticationException(USER_NOT_ACTIVATED, FORBIDDEN);

        return user;
    }

    @Override
    public String generateSocialAuthUrl(ProviderType providerType) {
        return switch (providerType) {
            case GOOGLE -> UriComponentsBuilder.fromHttpUrl(GOOGLE_AUTH_URI)
                    .queryParam("client_id", GOOGLE_CLIENT_ID)
                    .queryParam("redirect_uri", GOOGLE_REDIRECT_URI)
                    .queryParam("scope", GOOGLE_SCOPE)
                    .queryParam("response_type", "code")
                    .toUriString();
            case FACEBOOK -> UriComponentsBuilder.fromHttpUrl(FACEBOOK_AUTH_URI)
                    .queryParam("client_id", FACEBOOK_CLIENT_ID)
                    .queryParam("redirect_uri", FACEBOOK_REDIRECT_URI)
                    .queryParam("scope", FACEBOOK_SCOPE)
                    .queryParam("response_type", "code")
                    .toUriString();
        };
    }

    @Override
    public Map<String, Object> fetchSocialUser(String code, ProviderType providerType) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String accessToken;

        switch (providerType) {
            case GOOGLE -> {
                accessToken = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        GOOGLE_TOKEN_URI,
                        GOOGLE_CLIENT_ID,
                        GOOGLE_CLIENT_SECRET,
                        code,
                        GOOGLE_REDIRECT_URI
                ).getAccessToken();

                restTemplate.getInterceptors().add((request, body, execution) -> {
                    request.getHeaders().set("Authorization", "Bearer " + accessToken);
                    return execution.execute(request, body);
                });

                return new ObjectMapper().readValue(
                        restTemplate.getForEntity(GOOGLE_USER_INFO_URI, String.class).getBody(),
                        new TypeReference<>(){});
            }
            case FACEBOOK -> {
                String urlGetAccessToken = UriComponentsBuilder.fromHttpUrl(FACEBOOK_TOKEN_URI)
                        .queryParam("client_id", FACEBOOK_CLIENT_ID)
                        .queryParam("client_secret", FACEBOOK_CLIENT_SECRET)
                        .queryParam("redirect_uri", FACEBOOK_REDIRECT_URI)
                        .queryParam("code", code)
                        .toUriString();

                ResponseEntity<String> response = restTemplate.getForEntity(urlGetAccessToken, String.class);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                accessToken = jsonNode.get("access_token").asText();

                String userInfoUrl = UriComponentsBuilder.fromHttpUrl(FACEBOOK_USER_INFO_URI)
                        .queryParam("access_token", accessToken)
                        .toUriString();

                return objectMapper.readValue(
                        restTemplate.getForEntity(userInfoUrl, String.class).getBody(),
                        new TypeReference<>(){});
            }

            default ->
            {
                log.error("Provider not supported");
                throw new AuthenticationException(PROVIDER_NOT_SUPPORTED, BAD_REQUEST);
            }
        }

    }

    @Override
    public String generateToken(User user, boolean isRefresh) {
        JWSHeader accessHeader = new JWSHeader(ACCESS_TOKEN_SIGNATURE_ALGORITHM);
        JWSHeader refreshHeader = new JWSHeader(REFRESH_TOKEN_SIGNATURE_ALGORITHM);

        Date expiryTime = (isRefresh)
                ? new Date(Instant.now().plus(REFRESHABLE_DURATION, SECONDS).toEpochMilli())
                : new Date(Instant.now().plus(VALID_DURATION, SECONDS).toEpochMilli());

        String jwtID = UUID.randomUUID().toString();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("com.example.marketplace")
                .issueTime(new Date())
                .expirationTime(expiryTime)
                .jwtID(jwtID)
                .build();

        if (!isRefresh) {
            jwtClaimsSet = new JWTClaimsSet.Builder(jwtClaimsSet)
                    .claim("scope", buildScope(user))
                    .build();
        }

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = (isRefresh)
                ? new JWSObject(refreshHeader, payload)
                : new JWSObject(accessHeader, payload);

        try {
            if (isRefresh) {
                byte[] keyBytes = Base64.getDecoder().decode(REFRESH_SIGNER_KEY);
                jwsObject.sign(new MACSigner(keyBytes));
            } else {
                byte[] keyBytes = Base64.getDecoder().decode(ACCESS_SIGNER_KEY);
                jwsObject.sign(new MACSigner(keyBytes));
            }
            return jwsObject.serialize();

        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public User refresh(String refreshToken, HttpServletRequest servletRequest) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(refreshToken, true);
        String id = signedJWT.getJWTClaimsSet().getSubject();

        User user;
        try {
            user = userService.findById(id);

        } catch (AuthenticationException e) {
            throw new AuthenticationException(INVALID_TOKEN, BAD_REQUEST);
        }

        if (servletRequest.getHeader("Authorization") == null)
            throw new AuthenticationException(INVALID_TOKEN, BAD_REQUEST);

        String accessToken = servletRequest.getHeader("Authorization").substring(7);
        SignedJWT signedAccessTokenJWT = SignedJWT.parse(accessToken);
        String jwtID = signedAccessTokenJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedAccessTokenJWT.getJWTClaimsSet().getExpirationTime();

        if (!signedAccessTokenJWT.getJWTClaimsSet().getSubject().equals(id))
            throw new AuthenticationException(INVALID_TOKEN, BAD_REQUEST);

//        if (expiryTime.after(new Date())) {
//            baseRedisService.set(jwtID, "revoked");
//            baseRedisService.setTimeToLive(jwtID, expiryTime.getTime() - System.currentTimeMillis());
//        }

        return user;
    }

    @Override
    @Transactional
    public void sendEmailForgotPassword(String email) {
        sendEmail(email, RESET_PASSWORD);
    }

    @Override
    @Transactional
    public String forgotPassword(User user, String code) {
        Verification verification = verificationRepository.findByCode(code).orElseThrow(() ->
                new AuthenticationException(CODE_INVALID, BAD_REQUEST));

        if (verification.getExpiryTime().before(new Date()))
            throw new AuthenticationException(CODE_INVALID, UNPROCESSABLE_ENTITY);

        if (!verification.getUser().getEmail().equals(user.getEmail()))
            throw new AuthenticationException(CODE_INVALID, BAD_REQUEST);

        return verification.getToken();
    }

    @Override
    @Transactional
    public void resetPassword(String token, String password, String confirmationPassword) {
        Verification verification = verificationRepository.findById(token).orElseThrow(() ->
                new AuthenticationException(TOKEN_REVOKED, UNPROCESSABLE_ENTITY));

        if (verification.getExpiryTime().before(new Date()))
            throw new AuthenticationException(TOKEN_EXPIRED, UNPROCESSABLE_ENTITY);

        if (!password.equals(confirmationPassword))
            throw new AuthenticationException(PASSWORD_MIS_MATCH, BAD_REQUEST);

        if (isWeakPassword(password))
            throw new AuthenticationException(WEAK_PASSWORD, BAD_REQUEST);

        User user = verification.getUser();
        userService.updatePassword(user, password);
        verificationRepository.delete(verification);
    }

    @Override
    public void signOut(String accessToken, String refreshToken) throws ParseException, JOSEException {
        try {
            SignedJWT signAccessToken = verifyToken(accessToken, false);
            Date AccessTokenExpiryTime = signAccessToken.getJWTClaimsSet().getExpirationTime();

//            if (AccessTokenExpiryTime.after(new Date())) {
//                baseRedisService.set(signAccessToken.getJWTClaimsSet().getJWTID(), "revoked");
//                baseRedisService.setTimeToLive(signAccessToken.getJWTClaimsSet().getJWTID(),
//                        AccessTokenExpiryTime.getTime() - System.currentTimeMillis());
//            }

            SignedJWT signRefreshToken = verifyToken(refreshToken, true);
            Date RefreshTokenExpiryTime = signRefreshToken.getJWTClaimsSet().getExpirationTime();

//            if (RefreshTokenExpiryTime.after(new Date())) {
//                baseRedisService.set(signRefreshToken.getJWTClaimsSet().getJWTID(), "revoked");
//                baseRedisService.setTimeToLive(signRefreshToken.getJWTClaimsSet().getJWTID(),
//                        RefreshTokenExpiryTime.getTime() - System.currentTimeMillis());
//            }

        } catch (AuthenticationException exception) {
            log.error("Cannot sign out", exception);
            //TODO: Disable the user account
        }
    }

    @Transactional
    protected void sendEmail(String email, VerificationType verificationType) {
        User user = userService.findByEmail(email);

        Verification verification = verificationRepository.save(Verification.builder()
                .code(generateVerificationCode(6))
                .expiryTime(Date.from(Instant.now().plus(15, MINUTES)))
                .verificationType(verificationType)
                .user(user)
                .build());

        System.out.println(verification.toString());

//        kafkaTemplate.send(KAFKA_TOPIC_SEND_MAIL,
//                verificationType + ":" + email + ":" + verification.getToken() + ":" + verification.getCode());
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        System.out.println("=== DEBUG VERIFY TOKEN ===");
        System.out.println("Is Refresh: " + isRefresh);
        System.out.println("ACCESS_SIGNER_KEY: " + ACCESS_SIGNER_KEY);
        System.out.println("REFRESH_SIGNER_KEY: " + REFRESH_SIGNER_KEY);

        byte[] keyBytes = isRefresh
                ? Base64.getDecoder().decode(REFRESH_SIGNER_KEY)
                : Base64.getDecoder().decode(ACCESS_SIGNER_KEY);

        System.out.println("Key bytes length: " + keyBytes.length);
        System.out.println("Key bytes (Base64): " + Base64.getEncoder().encodeToString(keyBytes));

        JWSVerifier verifier = new MACVerifier(keyBytes);

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (isRefresh) {
            if (expiryTime.before(new Date()))
                throw new AuthenticationException(TOKEN_EXPIRED, UNAUTHORIZED);

            if (!verified)
                throw new AuthenticationException(INVALID_SIGNATURE, UNAUTHORIZED);

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    keyBytes,
                    REFRESH_TOKEN_SIGNATURE_ALGORITHM.getName()
            );
            try {
                NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                        .macAlgorithm(MacAlgorithm.from(REFRESH_TOKEN_SIGNATURE_ALGORITHM.getName()))
                        .build();
                nimbusJwtDecoder.decode(token);

            } catch (JwtException e) {
                throw new AuthenticationException(INVALID_SIGNATURE, UNAUTHORIZED);
            }

        } else {
            if (!verified || expiryTime.before(new Date()))
                throw new AuthenticationException(TOKEN_INVALID, UNAUTHORIZED);
        }

//        String value = (String) baseRedisService.get(signedJWT.getJWTClaimsSet().getJWTID());
//
//        if (value != null) {
//            if (value.equals("revoked"))
//                throw new AuthenticationException(TOKEN_REVOKED, UNAUTHORIZED);
//            else
//                throw new AuthenticationException(TOKEN_BLACKLISTED, UNAUTHORIZED);
//        }

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (user.getRole() != null) {
            stringJoiner.add("ROLE_" + user.getRole().getName());
        }

        return stringJoiner.toString();
    }

    public static String generateVerificationCode(int length) {
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }

        return code.toString();
    }

    private boolean isPasswordExpired(User user) {
        return false;
    }

    private boolean isTwoFactorRequired(User user) {
        return false;
    }

    private boolean isUserDisabled(User user) {
        return false;
    }

    private boolean isInvalidEmail(String email) {
        return false;
    }

    private boolean isWeakPassword(String password) {
        return false;
    }

}
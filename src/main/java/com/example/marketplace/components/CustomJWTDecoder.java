package com.example.marketplace.components;

import com.example.marketplace.exceptions.authenication.AuthenticationErrorCode;
import com.example.marketplace.exceptions.authenication.AuthenticationException;
import com.example.marketplace.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

import static com.example.marketplace.helpers.Constants.ACCESS_TOKEN_SIGNATURE_ALGORITHM;

@Component
@RequiredArgsConstructor
public class CustomJWTDecoder implements JwtDecoder {
    @Value("${jwt.accessSignerKey}")
    private String ACCESS_SIGNER_KEY;

    private final AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;


    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            if (!authenticationService.introspect(token)) throw new AuthenticationException(AuthenticationErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
        } catch (ParseException | JOSEException e) {
            throw new JwtException("Token parsing failed: "+e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(ACCESS_SIGNER_KEY.getBytes(), ACCESS_TOKEN_SIGNATURE_ALGORITHM.getName());

            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.from(ACCESS_TOKEN_SIGNATURE_ALGORITHM.getName()))
                    .build();
        }
        return nimbusJwtDecoder.decode(token);
    }
}


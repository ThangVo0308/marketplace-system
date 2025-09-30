package com.example.marketplace.configs;

import com.example.marketplace.components.CustomJWTDecoder;
import com.example.marketplace.components.JwtAuthenticationEntryPoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.stream.Stream;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {

        @Value("${api.prefix}")
        @NonFinal
        String API_PREFIX;

        CustomJWTDecoder customJwtDecoder;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        String[] PUBLIC_ENDPOINTS = Stream.of(
                "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/auth/verify-email-by-code",
                        "/auth/verify-email-by-token",
                        "/auth/send-email-verification",
                        "/auth/send-forgot-password",
                        "/auth/forgot",
                        "/auth/reset",
                        "/auth/sign-in",
                        "/auth/social/**",
                        "/auth/social/callback",
                        "/auth/sign-out",
                        "/auth/introspect",
                        "/users",
                        "/auth/**",
                        "/api/v1/auth/**"


                ).map(s -> (s.contains("api-docs") || s.contains("swagger")) ? s : API_PREFIX + s)
                .toList().toArray(new String[0]);

                httpSecurity.authorizeHttpRequests(request -> {
                        request.requestMatchers(PUBLIC_ENDPOINTS)
                                        .permitAll()
                                        .anyRequest()
                                        // .permitAll();
                                        .authenticated();

                });

                httpSecurity
                        .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt -> jwt
                                        .decoder(customJwtDecoder)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                )
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                        )
                        .csrf(AbstractHttpConfigurer::disable)
                        .cors(withDefaults())
                        .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                return httpSecurity.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration corsConfiguration = new CorsConfiguration();

                corsConfiguration.addAllowedOrigin("*");
                corsConfiguration.addAllowedMethod("*");
                corsConfiguration.addAllowedHeader("*");

                UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
                urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

                return urlBasedCorsConfigurationSource;
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

                return jwtAuthenticationConverter;
        }

}
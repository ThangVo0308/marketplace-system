package com.example.marketplace.dtos.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import io.micrometer.observation.annotation.Observed;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Observed
public class GoogleAuthorizationCodeTokenRequest {

    HttpTransport transport;

    String tokenUrl;

    String clientId;

    String clientSecret;

    String code;

    String redirectUri;

    public String getAccessToken() throws Exception {
        HttpRequestFactory requestFactory = transport.createRequestFactory();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", clientId);
        parameters.put("client_secret", clientSecret);
        parameters.put("code", code);
        parameters.put("redirect_uri", redirectUri);
        parameters.put("grant_type", "authorization_code");

        HttpRequest request = requestFactory.buildPostRequest(
                new GenericUrl(tokenUrl),
                new UrlEncodedContent(parameters)
        );

        String response = request.execute().parseAsString();

        // Lấy access_token từ JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("access_token").asText(); // Trả về giá trị access_token
    }
}
